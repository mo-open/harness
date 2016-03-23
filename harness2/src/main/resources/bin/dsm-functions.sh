#!/usr/bin/env bash
function runGrizzlememcached(){
   exe org.mds.harness2.tools.memcached.TestGrizzlyMemcachedPerf -df testMemcached.yml
}

function runHttpClientPerf(){
   exe org.mds.harness2.tools.httpbench.HttpBench -df httpBench.yml
}

function runMapDBPerf(){
   exe org.mds.harness2.tools.mapdb.TestMapDBPerf -df testMapDB.yml
}

function runMemcachedPerf(){
   exe org.mds.harness2.tools.memcached.TestMemcachedPerf -df testMemcached.yml
}

function runNativeDriverPerf(){
   exe org.mds.harness2.tools.mongo.NativeDriverTest -df testMongo.yml
}

function runSpringDataPerf(){
   exe org.mds.harness2.tools.mongo.SpringDataTest -df testMongo.yml
}

function runProcessorPerf(){
   exe org.mds.harness2.tools.processor.ProcessorPerf -df processor.yml
}

function runReactorPerf(){
   exe org.mds.harness2.tools.reactor.ReactorPerf -df reactor.yml
}

function runJedisPerf(){
   exe org.mds.harness2.tools.redis.TestJedisPerf -df redis-perf.yml
}

function runReflectPerf(){
   exe org.mds.harness2.tools.lang.TestReflect -df system.yml
}

function runRegexPerf(){
   exe org.mds.harness2.tools.lang.TestRegex -df testRegex.yml
}

function runXmemcachedPerf(){
   exe org.mds.harness2.tools.memcached.TestXMemcachedPerf -df testMemcached.yml
}