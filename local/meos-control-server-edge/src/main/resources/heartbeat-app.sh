#/bin/sh

check() {
  # shellcheck disable=SC1083
  response=`curl "${address}${url}" -X $request --connect-timeout 15 -m 20 -o /dev/null -s -w %{http_code}`

  if [ $response != '200' ];then
    exit 3
  fi
}

if [ -n "$SERVER_PORT" ];then
  port="$SERVER_PORT"
elif [ -n "$PORT_DOCKER" ];then
  port="$PORT_DOCKER"
fi

address="http://127.0.0.1:${port}"
request='GET'
url="/bizStaticType/getBasicsConfigType"

check
