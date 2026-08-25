#!/usr/bin/env bash
set -euo pipefail

group_id="com.github.RethinkQAQ.RethinkConfigUiLib"
group_path="${group_id//.//}"
artifact_base="rethink-config-ui-lib"
tag="${VERSION:?JitPack VERSION is not set}"
release_version="${tag#v}"
release_url="https://github.com/RethinkQAQ/RethinkConfigUiLib/releases/download/${tag}"
repository="${HOME}/.m2/repository/${group_path}"
artifact_dir="${PWD}/build/jitpack-artifacts"

mkdir -p "${artifact_dir}"

versions_line="$(sed -n 's/^stonecutter_enabled_versions=//p' gradle.properties)"
if [[ -z "${versions_line}" ]]; then
  echo "stonecutter_enabled_versions is missing from gradle.properties" >&2
  exit 1
fi

IFS=',' read -r -a versions <<< "${versions_line}"
for minecraft_version in "${versions[@]}"; do
  minecraft_version="$(printf '%s' "${minecraft_version}" | tr -d '[:space:]')"
  [[ -n "${minecraft_version}" ]] || continue

  artifact_id="${artifact_base}-mc${minecraft_version}"
  file_name="${artifact_base}-${release_version}-mc${minecraft_version}.jar"
  version_dir="${repository}/${artifact_id}/${tag}"
  target="${version_dir}/${artifact_id}-${tag}.jar"

  mkdir -p "${version_dir}"
  curl --fail --location --retry 3 --retry-delay 2 \
    "${release_url}/${file_name}" \
    --output "${target}"

  cat > "${version_dir}/${artifact_id}-${tag}.pom" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${group_id}</groupId>
  <artifactId>${artifact_id}</artifactId>
  <version>${tag}</version>
  <packaging>jar</packaging>
  <name>Rethink Config UI Lib for Minecraft ${minecraft_version}</name>
  <url>https://github.com/RethinkQAQ/RethinkConfigUiLib</url>
  <licenses>
    <license>
      <name>GNU Lesser General Public License v3.0 only</name>
      <url>https://www.gnu.org/licenses/lgpl-3.0.html</url>
    </license>
  </licenses>
</project>
EOF

  cp "${target}" "${artifact_dir}/${file_name}"
done
