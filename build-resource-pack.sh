#!/usr/bin/bash


if [[ -f pxnBackrooms-resourcepack.zip ]]; then
	\rm -fv  pxnBackrooms-resourcepack.zip  || exit 1
fi


# backrooms files
\pushd  "resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  ../pxnBackrooms-resourcepack.zip *  || exit 1
\popd >/dev/null

# more foods
\pushd  "../MoreFoods/resourcepack/"  >/dev/null  || exit 1
	\zip -r -9  ../../pxnBackrooms/pxnBackrooms-resourcepack.zip *  \
		--exclude pack.mcmeta  || exit 1
\popd >/dev/null


\sha1sum pxnBackrooms-resourcepack.zip > pxnBackrooms-resourcepack.sha1  || exit 1


\cp  pxnBackrooms-resourcepack.zip   resources/
\cp  pxnBackrooms-resourcepack.sha1  resources/
