/*
 * Copyright (c) 2020, ChenJun(powe4j@outlook.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.kit.seq;

import com.power4j.kit.seq.core.LongSeqPool;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 单机序号池测试
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
@State(Scope.Benchmark)
@Threads(Threads.MAX)
@Fork(value = 1, jvmArgsAppend = { "-server", "-Xms32m", "-Xmx128m", "-Xmn64m", "-XX:CMSInitiatingOccupancyFraction=82",
		"-Xss256k", "-XX:LargePageSizeInBytes=64m" })
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LongSeqPoolBench {

	private LongSeqPool longSeqPool;

	@Setup
	public void setup() {
		longSeqPool = LongSeqPool.forSize("longSeqPool", BenchParam.SEQ_INIT_VAL, BenchParam.SEQ_POOL_SIZE, true);
	}

	@Benchmark
	public void longSeqPoolTest() {
		longSeqPool.next();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LongSeqPoolBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}