#!/bin/bash

export TEST_MYSQL_URL="jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false"
export TEST_MYSQL_USER=root
export TEST_MYSQL_PWD=root
export TEST_REDIS_URI="redis://10.11.12.1:6379"
export TEST_MONGO_URI="mongodb://root:root@127.0.0.1:27017"


mvn test -DskipTests=false -B -P 'aliyun-repo,!oss-release,!travis-ci'