#!/usr/bin/env bash
if [[ "$TRAVIS_BRANCH" = 'master' ]] || [[ "$TRAVIS_BRANCH" =~ ^[0-9]+\.[0-9]+\.x$ ]] && [[ "$TRAVIS_PULL_REQUEST" == 'false' ]]; then
  openssl aes-256-cbc -K $encrypted_784c08ba1d52_key -iv $encrypted_784c08ba1d52_iv -in travis/codesigning.asc.enc -out travis/codesigning.asc -d
  gpg --fast-import travis/codesigning.asc
  mvn -V -B -s travis/settings.xml clean deploy -P sign -DskipTests
fi