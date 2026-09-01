#!/usr/bin/env bash
set -euo pipefail

group_id="com.github.RethinkQAQ.RethinkConfigUiLib"
group_path="${group_id//.//}"
tag="${VERSION:?JitPack VERSION is not set}"
release_base_url="${RCUI_RELEASE_BASE_URL:-https://github.com/RethinkQAQ/RethinkConfigUiLib/releases/download}"
release_url="${release_base_url%/}/${tag}"
repository="${HOME}/.m2/repository/${group_path}"
manifest="$(mktemp)"
trap 'rm -f "${manifest}"' EXIT

[[ "${tag}" == v[0-9]* ]] || { echo "VERSION must be a release tag such as v0.7.0" >&2; exit 1; }
curl --fail --location --retry 3 "${release_url}/rcui-release-manifest.txt" --output "${manifest}"
grep -q "^Tag: ${tag}$" "${manifest}" || { echo "Release manifest tag mismatch" >&2; exit 1; }

while read -r minecraft platform file sha256; do
  [[ -n "${minecraft}" && "${minecraft}" != Tag:* ]] || continue
  artifact_id="rethink-config-ui-lib-${minecraft}-${platform}"
  dir="${repository}/${artifact_id}/${tag}"
  mkdir -p "${dir}"
  jar="${dir}/${artifact_id}-${tag}.jar"
  curl --fail --location --retry 3 "${release_url}/${file}" --output "${jar}"
  printf '%s  %s\n' "${sha256}" "${jar}" | sha256sum -c -
  cat > "${dir}/${artifact_id}-${tag}.pom" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${group_id}</groupId>
  <artifactId>${artifact_id}</artifactId>
  <version>${tag}</version>
  <packaging>jar</packaging>
  <name>Rethink Config UI Lib for Minecraft ${minecraft} ${platform}</name>
  <licenses><license><name>GNU Lesser General Public License v3.0 only</name><url>https://www.gnu.org/licenses/lgpl-3.0.html</url></license></licenses>
</project>
EOF
done < <(tail -n +2 "${manifest}")
