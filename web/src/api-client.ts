export type ApiClientOptions = {
  apiUrl: string;
  accessToken: string;
  boxId?: string;
  onUnauthorized?: () => void;
};

type ApiErrorBody = {
  error?: string | { code?: string; message?: string };
  message?: string;
};

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export type ApiClient = {
  request<T>(path: string, init?: RequestInit): Promise<T>;
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
};

function safeError(response: Response, body: ApiErrorBody | null): ApiError {
  const structuredError = typeof body?.error === "object" ? body.error : undefined;
  const clientMessage = typeof body?.error === "string" ? body.error : structuredError?.message ?? body?.message;
  const message = response.status === 401
    ? "Your session expired. Sign in again."
    : response.status >= 500
      ? "The service could not complete the request. Try again."
    : clientMessage ?? `Request failed (${response.status}).`;
  return new ApiError(message, response.status, structuredError?.code);
}

export function createApiClient(options: ApiClientOptions): ApiClient {
  const apiUrl = options.apiUrl.replace(/\/+$/, "");

  async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
    if (!path.startsWith("/")) throw new Error("API paths must start with '/'.");
    const headers = new Headers(init.headers);
    headers.set("Authorization", `Bearer ${options.accessToken}`);
    if (options.boxId) headers.set("X-Box-Id", options.boxId);
    else headers.delete("X-Box-Id");
    if (init.body != null && !(init.body instanceof FormData) && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(`${apiUrl}${path}`, { ...init, headers });
    if (!response.ok) {
      if (response.status === 401) options.onUnauthorized?.();
      const contentType = response.headers.get("Content-Type") ?? "";
      const body = contentType.includes("application/json")
        ? await response.json().catch(() => null) as ApiErrorBody | null
        : null;
      throw safeError(response, body);
    }
    if (response.status === 204) return undefined as T;
    return await response.json() as T;
  }

  return {
    request,
    get: <T,>(path: string) => request<T>(path),
    post: <T,>(path: string, body?: unknown) => request<T>(path, {
      method: "POST",
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    }),
  };
}
