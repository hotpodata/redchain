package com.hotpodata.redchain.interfaces

import com.hotpodata.redchain.data.Chain

/**
 * Created by jdrotos on 11/6/15.
 */
interface ChainUpdateListener {
    fun onChainUpdated(chain: Chain)
}