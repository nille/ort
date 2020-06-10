/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.analyzer.managers

import org.ossreviewtoolkit.downloader.VersionControlSystem
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.yamlMapper
import org.ossreviewtoolkit.utils.normalizeVcsUrl
import org.ossreviewtoolkit.utils.test.DEFAULT_ANALYZER_CONFIGURATION
import org.ossreviewtoolkit.utils.test.DEFAULT_REPOSITORY_CONFIGURATION
import org.ossreviewtoolkit.utils.test.USER_DIR
import org.ossreviewtoolkit.utils.test.patchExpectedResult

import io.kotest.matchers.string.haveSubstring
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

import java.io.File

class PhpComposerTest : StringSpec() {
    private val projectsDir = File("src/funTest/assets/projects/synthetic/php-composer").absoluteFile
    private val vcsDir = VersionControlSystem.forDirectory(projectsDir)!!
    private val vcsRevision = vcsDir.getRevision()
    private val vcsUrl = vcsDir.getRemoteUrl()

    init {
        "Project dependencies are detected correctly" {
            val definitionFile = File(projectsDir, "lockfile/composer.json")

            val result = createPhpComposer().resolveSingleProject(definitionFile)
            val expectedResults = patchExpectedResult(
                File(projectsDir.parentFile, "php-composer-expected-output.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision,
                path = vcsDir.getPathToRoot(definitionFile.parentFile)
            )

            yamlMapper.writeValueAsString(result) shouldBe expectedResults
        }

        "Error is shown when no lockfile is present" {
            val definitionFile = File(projectsDir, "no-lockfile/composer.json")
            val result = createPhpComposer().resolveSingleProject(definitionFile)

            with(result) {
                project.id shouldBe Identifier(
                    "PhpComposer::src/funTest/assets/projects/synthetic/" +
                            "php-composer/no-lockfile/composer.json:"
                )
                project.definitionFilePath shouldBe
                        "analyzer/src/funTest/assets/projects/synthetic/php-composer/no-lockfile/composer.json"
                packages.size shouldBe 0
                issues.size shouldBe 1
                issues.first().message should haveSubstring("IllegalArgumentException: No lockfile found in")
            }
        }

        "No composer.lock is required for projects without dependencies" {
            val definitionFile = File(projectsDir, "no-deps/composer.json")

            val result = createPhpComposer().resolveSingleProject(definitionFile)
            val expectedResults = patchExpectedResult(
                File(projectsDir.parentFile, "php-composer-expected-output-no-deps.yml"),
                definitionFilePath = VersionControlSystem.getPathInfo(definitionFile).path,
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision,
                path = vcsDir.getPathToRoot(definitionFile.parentFile)
            )

            yamlMapper.writeValueAsString(result) shouldBe expectedResults
        }

        "No composer.lock is required for projects with empty dependencies" {
            val definitionFile = File(projectsDir, "empty-deps/composer.json")

            val result = createPhpComposer().resolveSingleProject(definitionFile)
            val expectedResults = patchExpectedResult(
                File(projectsDir.parentFile, "php-composer-expected-output-no-deps.yml"),
                definitionFilePath = VersionControlSystem.getPathInfo(definitionFile).path,
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision,
                path = vcsDir.getPathToRoot(definitionFile.parentFile)
            )

            yamlMapper.writeValueAsString(result) shouldBe expectedResults
        }

        "Packages defined as provided are not reported as missing" {
            val definitionFile = File(projectsDir, "with-provide/composer.json")

            val result = createPhpComposer().resolveSingleProject(definitionFile)
            val expectedResults = patchExpectedResult(
                File(projectsDir.parentFile, "php-composer-expected-output-with-provide.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision,
                path = vcsDir.getPathToRoot(definitionFile.parentFile)
            )

            yamlMapper.writeValueAsString(result) shouldBe expectedResults
        }

        "Packages defined as replaced are not reported as missing" {
            val definitionFile = File(projectsDir, "with-replace/composer.json")

            val result = createPhpComposer().resolveSingleProject(definitionFile)
            val expectedResults = patchExpectedResult(
                File(projectsDir.parentFile, "php-composer-expected-output-with-replace.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision,
                path = vcsDir.getPathToRoot(definitionFile.parentFile)
            )

            yamlMapper.writeValueAsString(result) shouldBe expectedResults
        }
    }

    private fun createPhpComposer() =
        PhpComposer("PhpComposer", USER_DIR, DEFAULT_ANALYZER_CONFIGURATION, DEFAULT_REPOSITORY_CONFIGURATION)
}
