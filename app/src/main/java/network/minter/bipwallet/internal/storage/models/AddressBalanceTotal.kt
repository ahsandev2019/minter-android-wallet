/*
 * Copyright (C) by MinterTeam. 2020
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package network.minter.bipwallet.internal.storage.models

import network.minter.core.crypto.MinterAddress
import network.minter.core.crypto.MinterPublicKey
import network.minter.explorer.models.AddressBalance
import network.minter.explorer.models.CoinDelegation
import org.parceler.Parcel
import java.math.BigDecimal


@Parcel
class AddressBalanceTotal : AddressBalance {
    @JvmField
    var delegated: BigDecimal = BigDecimal.ZERO

    @JvmField
    var delegatedCoins: MutableMap<MinterPublicKey, MutableList<CoinDelegation>> = HashMap()

    constructor(address: MinterAddress?) {
        this.address = address
        fillDefaultsOnEmpty()
    }

    constructor() {
        fillDefaultsOnEmpty()
    }

    constructor(source: AddressBalance, delegated: BigDecimal) {
        address = source.address
        coins = source.coins
        totalBalanceInBase = source.totalBalanceInBase
        totalBalanceInUSD = source.totalBalanceInUSD
        availableBalanceInBase = source.availableBalanceInBase
        availableBalanceInUSD = source.availableBalanceInUSD
        this.delegated = delegated
    }

    fun getDelegatedListByValidator(publicKey: MinterPublicKey): MutableList<CoinDelegation> {
        if (!delegatedCoins.containsKey(publicKey)) {
            return ArrayList(0)
        }

        return delegatedCoins[publicKey] ?: ArrayList(0)
    }

    fun getDelegatedByValidatorAndCoin(publicKey: MinterPublicKey?, coin: String?): CoinDelegation? {
        if (!hasDelegated(publicKey, coin)) return null

        return delegatedCoins[publicKey]!!.first { it.coin == coin }
    }

    fun hasDelegated(publicKey: MinterPublicKey?): Boolean {
        if (publicKey == null) return false
        return delegatedCoins.containsKey(publicKey) && delegatedCoins[publicKey] != null
    }

    fun hasDelegated(publicKey: MinterPublicKey?, coin: String?): Boolean {
        if (publicKey == null || coin == null) return false

        return delegatedCoins[publicKey]?.first { it.coin == coin } != null
    }
}