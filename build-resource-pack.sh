#!/usr/bin/bash
VERSION="{{{VERSION}}}"



NAME=""
while [ $# -gt 0 ]; do
	case "$1" in
	-N|--name)    \shift ;    NAME="$1"      ;;
	--name=*)                 NAME="${1#*=}" ;;
	-V|--version) \shift ; VERSION="$1"      ;;
	--version=*)           VERSION="${1#*=}" ;;
	*) echo "Unknown argument: $1" ; exit 1 ;;
	esac
	\shift
done

if [[ -z $NAME ]]; then
	echo "Resource pack name not provided"
	exit 1
fi
NAME="-${NAME}"

if [[ -z $VERSION ]] || [[ "$VERSION" == "{""{""{VERSION}""}""}" ]]; then
	VERSION=""
else
	VERSION="-${VERSION}"
fi



# remove old resource packs
\ls "./pxnBackrooms-resourcepack${NAME}"*.zip >/dev/null 2>/dev/null
if [[ $? -eq 0 ]]; then
	\rm -fv --preserve-root  "./pxnBackrooms-resourcepack${NAME}"*.zip  || exit 1
fi
\ls "./pxnBackrooms-resourcepack${NAME}"*.sha1 >/dev/null 2>/dev/null
if [[ $? -eq 0 ]]; then
	\rm -fv --preserve-root  "./pxnBackrooms-resourcepack${NAME}"*.sha1  || exit 1
fi
if [[ -f "./plugin/resources/pxnBackrooms-resourcepack${NAME}.zip" ]]; then
	\rm -fv --preserve-root  "./plugin/resources/pxnBackrooms-resourcepack${NAME}.zip"  || exit 1
fi
if [[ -f "./plugin/resources/pxnBackrooms-resourcepack${NAME}.sha1" ]]; then
	\rm -fv --preserve-root  "./plugin/resources/pxnBackrooms-resourcepack${NAME}.sha1"  || exit 1
fi



# common files
\pushd  "resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  || exit 1
\popd >/dev/null

# more foods
\pushd  "../MoreFoods/resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null

# redstone terminal
\pushd  "../RedstoneTerminal/resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null

# named files
\pushd  "resourcepack${NAME}/"  >/dev/null  || exit 1
	\zip -r -9  "../pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  || exit 1
\popd >/dev/null

# more foods
\pushd  "../MoreFoods/resourcepack${NAME}/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null

# redstone terminal
\pushd  "../RedstoneTerminal/resourcepack${NAME}/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${NAME}${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null



\sha1sum  "pxnBackrooms-resourcepack${NAME}${VERSION}.zip" \
	> "pxnBackrooms-resourcepack${NAME}${VERSION}.sha1"  || exit 1


\cp  "pxnBackrooms-resourcepack${NAME}${VERSION}.zip"   "resources/pxnBackrooms-resourcepack${NAME}.zip"
\cp  "pxnBackrooms-resourcepack${NAME}${VERSION}.sha1"  "resources/pxnBackrooms-resourcepack${NAME}.sha1"
