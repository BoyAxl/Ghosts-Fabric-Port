# Release Publishing

This repo publishes CurseForge files from GitHub Releases.

## One-time setup

1. Open the CurseForge API token page:

```text
https://www.curseforge.com/account/api-tokens
```

2. Log in if CurseForge asks you to.
3. Create or copy a CurseForge API token.
4. Open this GitHub repository.
5. Go to Settings -> Secrets and variables -> Actions.
6. Click New repository secret.
7. Use this name:

```text
CURSEFORGE_TOKEN
```

8. Paste the CurseForge token as the value.
9. Save it.

Do not put the token in a commit, release body, Discord message, or issue.

## Publishing a new version

1. Update `mod_version` in `gradle.properties`.

For example, if the previous Fabric port version was `0.1.4`, use:

```properties
mod_version=0.1.5
```

2. Commit and push the change.
3. Open GitHub -> Releases -> Draft a new release.
4. Create a new tag, for example:

```text
v0.1.5
```

5. Set the release title, for example:

```text
Ghosts 0.1.5 - Fabric 26.1.x (Unofficial Port)
```

6. Write the changelog in the release body.
7. Click Publish release.

GitHub Actions will build the mod as `ghosts-fabric-<version>+26.1.x.jar`, attach the jar to the GitHub Release, and upload the same jar to CurseForge. The GitHub Release body becomes the CurseForge changelog.

The mod metadata declares Minecraft compatibility as `~26.1`, so the same jar can load across the 26.1.x line when Mojang/Fabric do not introduce breaking changes. CurseForge game versions are explicit in `.github/workflows/publish-curseforge.yml`; add future 26.1.x patch versions there when CurseForge exposes them.

## If it fails

1. Open GitHub -> Actions.
2. Click Publish CurseForge Release.
3. Open the failed run.
4. Read the red step.
5. Fix the problem and use Re-run jobs.

Most failures are caused by a missing `CURSEFORGE_TOKEN`, an expired token, or a release version that already exists on CurseForge.
