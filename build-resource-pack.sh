#!/usr/bin/bash
VERSION="{{{VERSION}}}"


if [[ -z $VERSION ]] || [[ "$VERSION" == "\{\{\{VERSION\}\}\}" ]]; then
	VERSION=""
else
	VERSION="-${VERSION}"
fi


\ls "./pxnBackrooms-resourcepack"*.zip >/dev/null 2>/dev/null
if [[ $? -eq 0 ]]; then
	\rm -fv --preserve-root  "./pxnBackrooms-resourcepack"*.zip  || exit 1
fi
\ls "./pxnBackrooms-resourcepack"*.sha1 >/dev/null 2>/dev/null
if [[ $? -eq 0 ]]; then
	\rm -fv --preserve-root  "./pxnBackrooms-resourcepack"*.sha1  || exit 1
fi
if [[ -f "./plugin/resources/pxnBackrooms-resourcepack.zip" ]]; then
	\rm -fv --preserve-root  "./plugin/resources/pxnBackrooms-resourcepack.zip"  || exit 1
fi
if [[ -f "./plugin/resources/pxnBackrooms-resourcepack.sha1" ]]; then
	\rm -fv --preserve-root  "./plugin/resources/pxnBackrooms-resourcepack.sha1"  || exit 1
fi


# backrooms files
\pushd  "resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../pxnBackrooms-resourcepack${VERSION}.zip"  *  || exit 1
\popd >/dev/null
# more foods
\pushd  "../MoreFoods/resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null
# redstone terminal
\pushd  "../RedstoneTerminal/resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  "../../pxnBackrooms/pxnBackrooms-resourcepack${VERSION}.zip"  *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null


\sha1sum  "pxnBackrooms-resourcepack${VERSION}.zip" \
	> "pxnBackrooms-resourcepack${VERSION}.sha1"  || exit 1


\cp  "pxnBackrooms-resourcepack${VERSION}.zip"   "resources/pxnBackrooms-resourcepack.zip"
\cp  "pxnBackrooms-resourcepack${VERSION}.sha1"  "resources/pxnBackrooms-resourcepack.sha1"
