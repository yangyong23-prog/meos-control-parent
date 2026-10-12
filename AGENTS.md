<!-- TRELLIS:START -->
# Trellis Instructions

These instructions are for AI assistants working in this project.

This project is managed by Trellis. The working knowledge you need lives under `.trellis/`:

- `.trellis/workflow.md` — development phases, when to create tasks, skill routing
- `.trellis/spec/` — package- and layer-scoped coding guidelines (read before writing code in a given layer)
- `.trellis/workspace/` — per-developer journals and session traces
- `.trellis/tasks/` — active and archived tasks (PRDs, research, jsonl context)

If a Trellis command is available on your platform (e.g. `/trellis:finish-work`, `/trellis:continue`), prefer it over manual steps. Not every platform exposes every command.

If you're using Codex or another agent-capable tool, additional project-scoped helpers may live in:
- `.agents/skills/` — reusable Trellis skills
- `.codex/agents/` — optional custom subagents

Managed by Trellis. Edits outside this block are preserved; edits inside may be overwritten by a future `trellis update`.

<!-- TRELLIS:END -->

## Validation Concurrency

When sub-agents or other concurrent tasks are active, shared-resource validation is owned by the main session.

Sub-agents must not run:

- database-backed tests;
- real browser verification.

The main session runs shared-resource validation serially:

- do not run database validation and real browser validation at the same time;
- do not start either while another worker is already using that shared resource.

Sub-agents may run lint, typecheck, pure unit tests, mocked Nuxt/component tests, and static checks.

## Windows Encoding

This project contains Chinese Markdown, SQL, and configuration notes. When reading text files in Windows PowerShell, explicitly specify UTF-8 to avoid garbled Chinese output caused by different PowerShell versions or system code pages:

```powershell
Get-Content -Raw -Encoding UTF8 path\to\file.md
```

Prefer `rg` for text search. If Chinese text appears garbled in the terminal, treat it as a read-encoding issue first; do not rewrite the source file just because the terminal output is garbled.
