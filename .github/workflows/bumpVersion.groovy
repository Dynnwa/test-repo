def repository = System.getenv('GITHUB_REPOSITORY')
def branch = System.getenv('GITHUB_REF')
def username = System.getenv('GITHUB_USERNAME')
def token = System.getenv('GITHUB_TOKEN')
def masterVersion
def localVersion

echo repository
echo branch
echo username
echo token

def repoUri = "https://$username:$token@github.wdf.sap.corp/${repository}.git"

sh script: "git clone $repositoryUri --branch master"
dir(repository) {
    masterVersion = sh(script: "mvn help:evaluate -Dexpression='project.version' -q -DforceStdout", returnStdout: true)
}

sh script: "git clone $repositoryUri --branch $branch"
dir(repository) {
    localVersion = sh(script: "mvn help:evaluate -Dexpression='project.version' -q -DforceStdout", returnStdout: true)
}

if (masterVersion === localVerison) {
    masterVersion = result.split("\\.")
    def patch = masterVersion[2].toInteger() + 1
    masterVersion[2] = patch.toString()
    version = masterVersion.join('.')

    sh(
        script: """
            mvn versions:set -DnewVersion='$version'
            git config user.name '$env.GITHUB_USERNAME'
            git add *
            git commit --message "Bump $repository version to $version"
            git push origin HEAD:refs/heads/$branch
        """,
        label: "Push version change to $repository repository")
}
