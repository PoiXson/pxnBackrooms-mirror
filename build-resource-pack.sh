#!/usr/bin/bash


if [[ -f resources/pxnBackrooms-resourcepack.zip ]]; then
	\rm -fv  resources/pxnBackrooms-resourcepack.zip  || exit 1
fi


\pushd  "resources/pack/"  >/dev/null  || exit 1
#TODO: remove excludes
	\zip -r -9  ../pxnBackrooms-resourcepack.zip *  \
		--exclude */redstone_lamp.png     \
		--exclude */redstone_lamp_on.png  \
			|| exit 1
\popd >/dev/null


\sha1sum resources/pxnBackrooms-resourcepack.zip \
	> resources/pxnBackrooms-resourcepack.sha1  || exit 1
