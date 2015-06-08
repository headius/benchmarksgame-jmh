/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.headius;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.atomic.AtomicInteger;

public class fannkuch {

    @Benchmark
    public int fannkuch() {
        return new fannkuchredux().run();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(fannkuch.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    public final class fannkuchredux
    {
        private final int NCHUNKS = 240;
        private int CHUNKSZ;
        private int NTASKS;
        private int n;
        private int[] Fact;
        private int[] maxFlips;
        private int[] chkSums;
        private AtomicInteger taskId;

        int[] p, pp, count;

        public fannkuchredux()
        {
            // Inititalize
            n = 12;

            Fact = new int[n+1];
            Fact[0] = 1;
            for ( int i=1; i<Fact.length; ++i ) {
                Fact[i] = Fact[i-1] * i;
            }

            CHUNKSZ = (Fact[n] + NCHUNKS - 1) / NCHUNKS;
            NTASKS = (Fact[n] + CHUNKSZ - 1) / CHUNKSZ;
            maxFlips = new int[NTASKS];
            chkSums  = new int[NTASKS];
            taskId = new AtomicInteger(0);

            p     = new int[n];
            pp    = new int[n];
            count = new int[n+1];
        }

        void runTask( int task )
        {
            int idxMin = task*CHUNKSZ;
            int idxMax = Math.min( Fact[n], idxMin+CHUNKSZ );

            // first permutation
            for ( int i=0; i<n; ++i ) {
                p[i] = i;
            }
            for ( int i=n-1, idx=idxMin; i>0; --i ) { // probably small, but pre-load n?
                int d = idx / Fact[i];
                count[i] = d;
                idx = idx % Fact[i];

                System.arraycopy( p, 0, pp, 0, i+1 );
                for ( int j=0; j<=i; ++j ) {
                    p[j] = j+d <= i ? pp[j+d] : pp[j+d-i-1];
                }
            }

            int p0=0,p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0,p9=0,p10=0,p11=0;
            switch ( n ) {
                case 12:  p11 = p[11];
                case 11:  p10 = p[10];
                case 10:  p9 = p[9];
                case 9:   p8 = p[8];
                case 8:   p7 = p[7];
                case 7:   p6 = p[6];
                case 6:   p5 = p[5];
                case 5:   p4 = p[4];
                case 4:   p3 = p[3];
                case 3:   p2 = p[2];
                case 2:   p1 = p[1];
                case 1:   p0 = p[0];
            }

            int maxflips = 0;
            int chksum = 0;

            for ( int i=idxMin; i<idxMax; ++i ) {

                // count flips
                if ( p0 != 0 ) {
                    int pp0 = p0, pp1 = p1, pp2 = p2, pp3 = p3, pp4 = p4, pp5 = p5,
                            pp6 = p6, pp7 = p7, pp8 = p8, pp9 = p9, pp10 = p10, pp11 = p11;
                    int flips = 1;
                    for ( ;; ++flips ) { // vectorization problems in this loop?
                        int t = pp0;
                        switch ( t ) {
                            case 1: pp0 = pp1; pp1 = t; break;
                            case 2: pp0 = pp2; pp2 = t; break;
                            case 3: pp0 = pp3; pp3 = t;
                                t = pp2; pp2 = pp1; pp1 = t;
                                break;
                            case 4: pp0 = pp4; pp4 = t;
                                t = pp3; pp3 = pp1; pp1 = t;
                                break;
                            case 5: pp0 = pp5; pp5 = t;
                                t = pp4; pp4 = pp1; pp1 = t;
                                t = pp3; pp3 = pp2; pp2 = t;
                                break;
                            case 6: pp0 = pp6; pp6 = t;
                                t = pp5; pp5 = pp1; pp1 = t;
                                t = pp4; pp4 = pp2; pp2 = t;
                                break;
                            case 7: pp0 = pp7; pp7 = t;
                                t = pp6; pp6 = pp1; pp1 = t;
                                t = pp5; pp5 = pp2; pp2 = t;
                                t = pp4; pp4 = pp3; pp3 = t;
                                break;
                            case 8: pp0 = pp8; pp8 = t;
                                t = pp7; pp7 = pp1; pp1 = t;
                                t = pp6; pp6 = pp2; pp2 = t;
                                t = pp5; pp5 = pp3; pp3 = t;
                                break;
                            case 9: pp0 = pp9; pp9 = t;
                                t = pp8; pp8 = pp1; pp1 = t;
                                t = pp7; pp7 = pp2; pp2 = t;
                                t = pp6; pp6 = pp3; pp3 = t;
                                t = pp5; pp5 = pp4; pp4 = t;
                                break;
                            case 10: pp0 = pp10; pp10 = t;
                                t = pp9; pp9 = pp1; pp1 = t;
                                t = pp8; pp8 = pp2; pp2 = t;
                                t = pp7; pp7 = pp3; pp3 = t;
                                t = pp6; pp6 = pp4; pp4 = t;
                                break;
                            case 11: pp0 = pp11; pp11 = t;
                                t = pp10; pp10 = pp1; pp1 = t;
                                t = pp9; pp9 = pp2; pp2 = t;
                                t = pp8; pp8 = pp3; pp3 = t;
                                t = pp7; pp7 = pp4; pp4 = t;
                                t = pp6; pp6 = pp5; pp5 = t;
                                break;
                        }
                        if ( pp0 == 0 ) break;
                    }

                    maxflips = Math.max( maxflips, flips );
                    chksum += (i&1)==0 ? flips : -flips;
                }

                // next permutation
                int t = p0; p0 = p1; p1 = t;
                int k=1;
                while ( ++count[k] > k ) {
                    count[k++] = 0;
                    switch ( k ) {
                        case 11: t = p11; p11 = p0; p0 = t;
                        case 10: t = p10; p10 = p0; p0 = t;
                        case 9:  t = p9; p9 = p0; p0 = t;
                        case 8:  t = p8; p8 = p0; p0 = t;
                        case 7:  t = p7; p7 = p0; p0 = t;
                        case 6:  t = p6; p6 = p0; p0 = t;
                        case 5:  t = p5; p5 = p0; p0 = t;
                        case 4:  t = p4; p4 = p0; p0 = t;
                        case 3:  t = p3; p3 = p0; p0 = t;
                        case 2:  t = p2; p2 = p0; p0 = p1; p1 = t;
                    }
                }
            }

            maxFlips[task] = maxflips;
            chkSums[task]  = chksum;
        }

        public int run()
        {
            int task;
            while ( ( task = taskId.getAndIncrement() ) < NTASKS ) {
                runTask( task );
            }

            // Reduce the results
            int res = 0;
            for ( int v : maxFlips ) {
                res = Math.max( res, v );
            }
            int chk = 0;
            for ( int v : chkSums ) {
                chk += v;
            }

            return res + chk;
        }
    }

}
