#!/bin/bash

#
# travis-build.sh - A script to build and/or release SciJava-based projects.
#

dir="$(dirname "$0")"

success=0
checkSuccess() {
	# Log non-zero exit code.
	test $1 -eq 0 || echo "==> FAILED: EXIT CODE $1" 1>&2

	# Record the first non-zero exit code.
	test $success -eq 0 && success=$1
}

# Build Maven projects.
if [ -f pom.xml ]
then
	echo travis_fold:start:scijava-maven
	echo "= Maven build ="
	echo
	echo "== Configuring Maven =="

	# NB: Suppress "Downloading/Downloaded" messages.
	# See: https://stackoverflow.com/a/35653426/1207769
	export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

	# Populate the settings.xml configuration.
	mkdir -p "$HOME/.m2"
	settingsFile="$HOME/.m2/settings.xml"
	customSettings=.travis/settings.xml
	if [ -f "$customSettings" ]
	then
		cp "$customSettings" "$settingsFile"
	else
		cat >"$settingsFile" <<EOL
<settings>
	<servers>
		<server>
			<id>imagej.releases</id>
			<username>travis</username>
			<password>\${env.MAVEN_PASS}</password>
		</server>
		<server>
			<id>imagej.snapshots</id>
			<username>travis</username>
			<password>\${env.MAVEN_PASS}</password>
		</server>
		<server>
			<id>sonatype-nexus-releases</id>
			<username>scijava-ci</username>
			<password>\${env.OSSRH_PASS}</password>
		</server>
	</servers>
EOL
		# NB: Use maven.imagej.net as sole mirror if defined in <repositories>.
		# This hopefully avoids intermittent "ReasonPhrase:Forbidden" errors
		# when the Travis build pings Maven Central; see travis-ci/travis-ci#6593.
		grep -A 2 '<repository>' pom.xml | grep -q 'maven.imagej.net' &&
		cat >>"$settingsFile" <<EOL
	<mirrors>
		<mirror>
			<id>imagej-mirror</id>
			<name>ImageJ mirror</name>
			<url>https://maven.imagej.net/content/groups/public/</url>
			<mirrorOf>*</mirrorOf>
		</mirror>
	</mirrors>
EOL
		cat >>"$settingsFile" <<EOL
	<profiles>
		<profile>
			<id>gpg</id>
			<activation>
				<file>
					<exists>\${env.HOME}/.gnupg</exists>
				</file>
			</activation>
			<properties>
				<gpg.keyname>\${env.GPG_KEY_NAME}</gpg.keyname>
				<gpg.passphrase>\${env.GPG_PASSPHRASE}</gpg.passphrase>
			</properties>
		</profile>
	</profiles>
</settings>
EOL
	fi

	# Install GPG on OSX/macOS
	if [ "$TRAVIS_OS_NAME" = osx ]
	then
		HOMEBREW_NO_AUTO_UPDATE=1 brew install gnupg2
	fi

	# Import the GPG signing key.
	keyFile=.travis/signingkey.asc
	key=$1
	iv=$2
	if [ "$key" -a "$iv" -a -f "$keyFile.enc" ]
	then
		# NB: Key and iv values were given as arguments.
		echo
		echo "== Decrypting GPG keypair =="
		openssl aes-256-cbc -K "$key" -iv "$iv" -in "$keyFile.enc" -out "$keyFile" -d
		checkSuccess $?
	fi
	if [ "$TRAVIS_SECURE_ENV_VARS" = true \
		-a "$TRAVIS_PULL_REQUEST" = false \
		-a -f "$keyFile" ]
	then
		echo
		echo "== Importing GPG keypair =="
		gpg --batch --fast-import "$keyFile"
		checkSuccess $?
	fi

	# Run the build.
	if [ "$TRAVIS_SECURE_ENV_VARS" = true \
		-a "$TRAVIS_PULL_REQUEST" = false \
		-a "$TRAVIS_BRANCH" = master ]
	then
		echo
		echo "== Building and deploying master SNAPSHOT =="
		mvn -B -Pdeploy-to-imagej deploy
		checkSuccess $?
	elif [ "$TRAVIS_SECURE_ENV_VARS" = true \
		-a "$TRAVIS_PULL_REQUEST" = false \
		-a -f release.properties ]
	then
		echo
		echo "== Cutting and deploying release version =="
		mvn -B release:perform
		checkSuccess $?
	else
		echo
		echo "== Building the artifact locally only =="
		mvn -B install javadoc:javadoc
		checkSuccess $?
	fi
	echo travis_fold:end:scijava-maven
fi

# Configure conda environment, if one is needed.
if [ -f environment.yml ]
then
	echo travis_fold:start:scijava-conda
	echo "= Conda setup ="

	condaDir=$HOME/miniconda
	if [ ! -f "$condaDir/bin/conda" ]; then
		echo
		echo "== Installing conda =="
		if [ "$TRAVIS_PYTHON_VERSION" = "2.7" ]; then
			wget https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh -O miniconda.sh
		else
			wget https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh -O miniconda.sh
		fi
		rm -rf "$condaDir"
		bash miniconda.sh -b -p "$condaDir"
		checkSuccess $?
	fi

	echo
	echo "== Updating conda =="
	$condaDir/bin/conda config --set always_yes yes --set changeps1 no &&
	$condaDir/bin/conda update -q conda &&
	$condaDir/bin/conda info -a
	checkSuccess $?

	echo
	echo "== Configuring environment =="
	condaEnv=travis-scijava
	test -d "$condaDir/envs/$condaEnv" && condaAction=update || condaAction=create
	$condaDir/bin/conda env "$condaAction" -n "$condaEnv" -f environment.yml &&
	$condaDir/bin/conda activate "$condaEnv"
	checkSuccess $?

	echo travis_fold:end:scijava-conda
fi

# Execute Jupyter notebooks.
if which jupyter >/dev/null 2>/dev/null
then
	echo travis_fold:start:scijava-jupyter
	echo "= Jupyter notebooks ="
	# NB: This part is fiddly. We want to loop over files even with spaces,
	# so we use the "find ... -print0 | while read $'\0' ..." idiom.
	# However, that runs the piped expression in a subshell, which means
	# that any updates to the success variable will not persist outside
	# the loop. So we suppress all stdout inside the loop, echoing only
	# the final value of success upon completion, and then capture the
	# echoed value back into the parent shell's success variable.
	success=$(find . -name '*.ipynb' -print0 | {
		while read -d $'\0' nbf
		do
			echo 1>&2
			echo "== $nbf ==" 1>&2
			jupyter nbconvert --execute --stdout "$nbf" >/dev/null
			checkSuccess $?
		done
		echo $success
	})
	echo travis_fold:end:scijava-jupyter
fi

exit $success
