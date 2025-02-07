/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Segment.io, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.kotlin_sample

import android.app.Application
import android.util.Log
import com.segment.analytics.Analytics
import com.segment.analytics.Middleware
import com.segment.analytics.ValueMap
import com.segment.analytics.integrations.BasePayload
import com.segment.analytics.integrations.TrackPayload
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class SampleApp : Application() {

    // https://app.segment.com/segment-joey-ng/sources/android/overview
    private val ANALYTICS_WRITE_KEY: String = "YSglowRTCTB56g5Bdt96oiHp7yZUH7qE"
    override fun onCreate() {
        super.onCreate()

        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/CircularStd-Book.otf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )

        // Initialize a new instance of the Analytics client.
        val builder = Analytics.Builder(this, ANALYTICS_WRITE_KEY)
            .experimentalNanosecondTimestamps()
            .trackApplicationLifecycleEvents()
            .defaultProjectSettings(
                ValueMap()
                    .putValue(
                        "integrations",
                        ValueMap()
                            .putValue(
                                "adjust",
                                ValueMap()
                                    .putValue("appToken", "<>")
                                    .putValue(
                                        "trackAttributionData",
                                        true
                                    )
                            )
                    )
            )
            .useSourceMiddleware(
                Middleware { chain ->
                    if (chain.payload().type() == BasePayload.Type.track) {
                        val payload = chain.payload() as TrackPayload
                        if (payload.event()
                            .equals("Button B Clicked", ignoreCase = true)
                        ) {
                            chain.proceed(payload.toBuilder().build())
                            return@Middleware
                        }
                    }
                    chain.proceed(chain.payload())
                }
            )
            .useDestinationMiddleware(
                "Segment.io",
                Middleware { chain ->
                    if (chain.payload().type() == BasePayload.Type.track) {
                        val payload = chain.payload() as TrackPayload
                        if (payload.event()
                            .equals("Button B Clicked", ignoreCase = true)
                        ) {
                            chain.proceed(payload.toBuilder().build())
                            return@Middleware
                        }
                    }
                    chain.proceed(chain.payload())
                }
            )
            .flushQueueSize(1)
            .recordScreenViews()
            .build()

        Analytics.setSingletonInstance(builder)

        val analytics = Analytics.with(this)

        analytics.onIntegrationReady(
            "Segment.io",
            Analytics.Callback<Any?> {
                Log.d("Segment Sample", "Segment integration ready.")
            }
        )
    }
}
