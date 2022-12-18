#!/usr/bin/bash


if [[ -f resources/pxnBackrooms-resourcepack.zip ]]; then
	\rm -fv  resources/pxnBackrooms-resourcepack.zip  || exit 1
fi


\pushd  "resources/pack/"  >/dev/null  || exit 1
	\zip -r -9  ../pxnBackrooms-resourcepack.zip *  \
			|| exit 1
\popd >/dev/null


\pushd  "resources/"  >/dev/null  || exit 1
	\sha1sum pxnBackrooms-resourcepack.zip \
		> pxnBackrooms-resourcepack.sha1   \
			|| exit 1
\popd >/dev/null
