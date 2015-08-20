#!/usr/bin/env bash
function runGrizzlememcached(){
   exe org.mds.harness2.tools.memcached.TestGrizzlyMemcachedPerf -df testMemcached.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.httpbench.HttpBench -df httpBench.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.mapdb.TestMapDBPerf -df testMapDB.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.memcached.TestMemcachedPerf -df testMemcached.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.mongo.NativeDriverTest -df testMongo.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.mongo.SpringDataTest -df testMongo.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.processor.ProcessorPerf -df processor.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.reactor.ReactorPerf -df reactor.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.redis.TestJedisPerf -df redis-perf.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.lang.TestReflect -df system.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.lang.TestRegex -df testRegex.yml
}

function runHttpClient(){
   exe org.mds.harness2.tools.memcached.TestXMemcachedPerf -df testMemcached.yml
}