## Git Commit Guidelines

Please make sure that git commits
  - are [atomic](https://en.wikipedia.org/wiki/Atomic_commit#Atomic_commit_convention), i.\,e. never mix code reformatting, code moves and code changes or commit multiple features at once.
  - are clean and complete, i.\,e. the code builds without errors or warnings or test failures, and provides documentation and tests for the new feature.
  - Please provide a git message that explains what you've done and why (see below)

### Commit-Messages

A git commit message should explain concisely what was done and why, with justification and reasoning.
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

