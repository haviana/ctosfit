# AI-Assisted Development

Ctosfit adopts a curated set of engineering agents, skills, standards and workflows from [AI Development Umbrella](https://github.com/haviana/ai-development-umbrella), adapted to this product.

## How to use
1. Start with the root [AGENTS.md](../../AGENTS.md).
2. Select relevant role definitions in [agents](../../agents/README.md).
3. Follow the task playbook in [skills](../../skills/README.md).
4. Apply [engineering standards](../../standards/engineering-principles.md) and [governance](../../governance/approval-workflow.md).
5. Open a PR with scope, rationale, tests, risks and rollback notes.

## Safety and approval
AI agents may prepare changes and PRs but cannot approve their own work, merge, bypass checks or perform consequential production actions without explicit human approval. GitHub Actions must use least privilege and must not execute untrusted PR code in privileged contexts.

## Configuration
The AI review workflow is advisory and requires an `OPENAI_API_KEY` repository Actions secret. This key is separate from a ChatGPT subscription. If the secret is absent, the workflow is skipped with a notice; ordinary quality and security workflows remain usable without it.
