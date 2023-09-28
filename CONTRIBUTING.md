[ISSUE-TRACKER]: https://github.com/EFS-OpenSource/superb-data-kraken-frontend/issues
[good first issue]: https://github.com/bitcoin/bitcoin/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22
[contacting us]: mailto:project-email@example.com

# Contributing to Superb Data Kraken (SDK)

The Superb Data Kraken (SDK) operates an open contribution model where everyone may contribute
in development, reviewing and testing.
We value the contributions of our  
community members, and your help is essential to making this project even better. This document presents guidelines and best practices on how you can contribute to the project.

## Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Reporting Bugs and Requesting Features](#reporting-bugs-and-requesting-features)
- [Contributing Code](#contributing-code)
- [Review Process](#review-process)
- [Decision Process](#decision-process)
- [Legal Compliance](#legal-compliance)
- [Conclusion](#conclusion)

## Introduction

Contributions in form of tests, [peer reviews](#review-process) and [code](#contributing-code) are welcome and needed.
Testing and reviewing tasks are highly appreciated and a good starting point to familiarize oneself with the project.
In addition, there are issues with the [good first issue] label as a starting point.
If you are interested in an issue, it is good to leave a comment to make sure the issue is still applicable and to inform others that you plan to address it.


In addition to contributors, there are repository maintainers who are responsible for
merging pull requests, the [release cycle](/docs/release-process.md), and
moderation.


When contributing, please follow the general [Contribution guidelines to Open Source Projects](http://www.contribution-guide.org/#), in particular concerning
- bugs submission
- general licensing rules
- version control, branching
  (But: New features should branch off of the ‘development’ branch instead of the 'main' branch.)
- code formatting
- obligatory tests
- obligatory documentation

Some of these rules are explained in detail below.



## Getting Started

To contribute to Superb Data Kraken, follow these steps:

1. Fork the repository on GitHub ([only for the first time](https://docs.github.com/en/get-started/quickstart/fork-a-repo))
2. Clone your forked repository to your local machine.

For more information see [Git Manual](https://git-scm.com/doc).
Please follow the documentation README.md carefully on how to set up the development environment and provide tests.

## Reporting Bugs and Requesting Features

If you encounter a bug or would like to request a new feature, please open an issue on our [Issue Tracker] and follow the provided template.



## Contributing Code

The workflow to submit changes is as follows:
1. Create topic branch
   Check out a new branch based on the development branch (see [branching conventions](#branching)) according to the [branch naming conventions]():
- Example:
  ````
  $ git checkout -b BRANCH_NAME
  ````
  If you get an error, you may need to fetch first by using
  ````
  $ git remote update && git fetch
  ````
- Use one branch per fix / feature
1. contribute code
- To maintain consistency in our codebase, please follow coding standards and best practices.
  -- fulfill the coding guideline standards
  -- contain the standard SDK Apache License 2.0 header in all files
1. Commit patches
- Please make sure that git commits
  -- are [atomic](https://en.wikipedia.org/wiki/Atomic_commit#Atomic_commit_convention), i.\,e. never mix code reformatting, code moves and code changes or commit multiple features at once.
  -- are clean and complete, i.\,e. the code builds without errors or warnings or test failures, and provides documentation and tests for the new feature.
  - Please provide a git message that explains what you've done and why and conforms to [commit-message conventions]()
- Commit your changes to your local repository
- Example:
  ````
  $ git commit
  ````
- Push to the remote branch
  - Example:
    ````
    $ git push origin BRANCH_NAME
    ````
1. Make a Merge Request / Pull request to submit your changes for review
- Make sure you send the PR to the <code>development</code> branch
- Don't forget to link PR to issue if you are solving one.
- The title and body of the pull request should follow a similar convention as for a commit message and
  contain sufficient description of what was done and why.
- Please indicate if a pull request is not to be considered for merging (yet), e.g. by using [Tasks Lists](https://docs.github.com/en/github/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#task-lists)
  in the body of the pull request to indicate tasks are pending.
- Assign the request to a maintainer
1. Address Feedback during [peer review](#review-process)
- you can add more commits to your pull request by committing them locally and pushing
  to your fork
  - Please reply to any review comments before your pull request is
    merged
    - if there is outstanding feedback, and you are not actively working on it, your pull request may be closed.


### Conventions

#### Branching

Base each development is ```main```-Branch. This portraits a stable, tested status. The commits of this branch hold tags for the according versions.

Based on the ```main```-Branch a branch ```develop``` will be created, which is base for single features and will be merged into the main with each new version. Tags with the according versions will be created.

Each new feature will be developed in a ```feature```-Branch, which holds each commit for its development. After the development a review will occur, if everything is fine, it will be merged into the ```develop```-Branch. Please consider Namingconventions.

If there are any bugs detected in the ```main```-Branch, fixes should be made in a ```hotfix```-Branch. This will be created based on the main-Branch and will be merged into develop and subsequently into the main. Please consider Namingconventions.

![Branching-Modell](docs/images/SDK_Branching_Modell.png)

#### Naming

| branch-type | example                 | description                                                             |
|-------------|-------------------------|-------------------------------------------------------------------------|
| feature     | f/123-short-description | branch contains implementation of feature 123, with a short description |
| hotfix      | h/566-short-description | branch contains fix of bug 566, with a short description                |
| develop     | develop                 | a repository's development-branch                                       |
| main        | main                    | a repository's main-branch                                              |

In order to provide a fast identification, whether a feature or bugfix is being handled in a branch, naming-conventions are required.

This also enables filtering via git e.g.

```git branch --list 'f*'```

Also, it enables a CI/CD-toolchange automated processing, which can lead to varying actions based on the branch-type.



#### Commit-Messages

A git commit message should be explain concisely what was done and why, with justification and reasoning.
and follow the [seven rules of a great Git commit message](https://chris.beams.io/posts/git-commit/)
1. Separate subject from body with a blank line
2. Limit the subject line to 50 characters
3. Capitalize the subject line
4. Do not end the subject line with a period
5. Use the imperative mood in the subject line
6. Wrap the (optional) body at 72 characters
7. Use the (optional) body to explain what and why vs. how

If the title alone is self-explanatory (like "Correct typo in init.cpp"), a single title line is sufficient.
Do not make any username `@` mentions.

This structure provides the possibility to automatically generate changelogs.
If a particular commit references an issue, please add the reference, e.g. `refs #1234` or `fixes #1234` or `closes #1234`, as this provides the
possibility to automatically close the corresponding issue when the pull request is merged.

In order to adhere to this structure, it is helpful to use a commit-template.
Please edit your .gitconfig as follows:

```
[commit]
template = ~/.gitmessage
```
and provide a .gitmessage in your user-home.

Here is an example of a .gitmessage-file in SDK-environment:

```
<type>: <description>
#-------------- 50 characters ------------------|
 
ADDED:
-
 
CHANGED:
-
 
DELETED:
-
 
 
REFS: <storynumber>
 
 
#--------------- 72 characters ---------------------------------------|
 
# DESCRIPTION
#
# <type>:
#
# feat:     (new feature for the user, not a new feature for build script)
# fix:      (bug fix for the user, not a fix to a build script)
# docs:     (changes to the documentation)
# style:    (formatting, missing semi colons, etc; no production code change)
# refactor: (refactoring production code, eg. renaming a variable)
# test:     (adding missing tests, refactoring tests; no production code change)
#
```

For more information, see [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/).



## Review Process

Anyone may participate in peer review and comment pull requests. There are some specific terms to be used:

* `Concept (N)ACK` means "I do (not) agree with the general goal"
* `Approach (N)ACK` means `Concept ACK`, but "I do (not) agree with the
  approach".

A `NACK` needs to include an explanation.

Code review starts with conceptual agreement on the change.
Please explain how the review was done, e.g.:
- "I have tested the code", and if not obvious describe how.
- "I have reviewed the code and it looks OK"

## Decision Process
The decision whether a pull request is merged into develop/main rests with the project merge
maintainers.

They will take the following criteria into consideration:
- clear use-case that serves the greater good of the project
- thorough peer-review
- code quality and adherence to project standards
- clear and concise documentation when behaviour of code changes
- unit tests, functional tests where appropriate
  (Where bugs are fixed, where possible, there should be unit tests
  demonstrating the bug and also proving the fix. This helps prevent regression.)
- Passing tests and no regressions.
- Compatibility with existing features.


## Legal Compliance

### Avoiding Copied Code

We value original and legally compliant contributions to this project. To ensure that we respect intellectual property
rights and maintain compliance with software licenses, we kindly request that all contributors refrain from checking in
copied code, including code from third-party sources, without the appropriate permissions or licenses.

### Licensing

By contributing to this project, you agree that your contributions will be subject to the [project's license](LICENSE)
and will comply with the following:


- Any code you submit must be your original work, or you must have the necessary permissions to contribute it.
- If you include code from other sources (e.g., libraries, frameworks, or open-source projects),
- ensure that the code is properly attributed, and its licensing terms are compatible with this project's licensing.


### Reporting Copyright Violations

If you suspect that any contributed code violates copyright or licensing agreements, please promptly notify the
project maintainers by opening an issue on [Issue Tracker] or [contacting us].



## Conclusion

Thank you for considering contributing to Superb Data Kraken! Your contributions are greatly appreciated, and they help make this project better for everyone. Get started today and be part of our open-source community!
