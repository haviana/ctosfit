export type AppRoute = "home" | "coach" | "athlete" | "not-found";

export function resolveAppRoute(pathname: string): AppRoute {
  const path = pathname.length > 1 ? pathname.replace(/\/+$/, "") : pathname;
  if (path === "/") return "home";
  if (path === "/coach") return "coach";
  if (path === "/athlete") return "athlete";
  return "not-found";
}

export function resolveRouteForRender(pathname: string, isOidcCallback: boolean, returnPath: string): AppRoute {
  return resolveAppRoute(isOidcCallback ? returnPath : pathname);
}
