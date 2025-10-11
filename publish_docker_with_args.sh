if [ -z "$1" ]; then
    echo "Error: version_code is required."
    exit 1
fi

build_version=$(./extract_version.sh)
version_code=$1

docker build --platform linux/amd64 --build-arg versionCode="$build_version" -t ghcr.io/rankademy/rankademy-server:"$version_code" .
docker push ghcr.io/rankademy/rankademy-server:"$version_code"
