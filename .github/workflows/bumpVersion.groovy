def GITHUB_REPOSITORY = System.getenv('GITHUB_REPOSITORY')
def repository = GITHUB_REPOSITORY.split('/')[1]
def username = GITHUB_REPOSITORY.split('/')[0]
def branch = System.getenv('GITHUB_REF')
def token = System.getenv('GITHUB_TOKEN')
def masterVersion
def localVersion

def repoUri = "https://$username:$token@github.wdf.sap.corp/$username/${repository}.git"

def command = "git clone $repoUri --branch master"
def process = command.execute()
process.waitFor()
println "Exit code: ${process.exitValue()}"

dir(repository) {
    masterVersion = sh(script: "mvn help:evaluate -Dexpression='project.version' -q -DforceStdout", returnStdout: true)
}

sh script: "git clone $repoUri --branch $branch"
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
