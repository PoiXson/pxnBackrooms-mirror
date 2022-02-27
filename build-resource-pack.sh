#!/usr/bin/bash


if [[ -f resources/pxnBackrooms-resourcepack.zip ]]; then
	\rm -fv  resources/pxnBackrooms-resourcepack.zip  || exit 1
fi


\pushd  "resources/pack/"  >/dev/null  || exit 1
	\zip -r -9  ../pxnBackrooms-resourcepack.zip *  || exit 1
\popd >/dev/null


\sha1sum resources/pxnBackrooms-resourcepack.zip \
	> resources/pxnBackrooms-resourcepack.sha1  || exit 1
