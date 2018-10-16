package com.hedera.contracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hedera.account.AccountCreate;
import com.hedera.file.FileCreate;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.file.HederaFile;
import com.hedera.utilities.ExampleUtilities;

public final class SimpleCallContract {
	final static Logger logger = LoggerFactory.getLogger(SimpleCallContract.class);

	public static void main(String... arguments) throws Exception {

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

		long shardNum = 0;
		long realmNum = 0;
		long contractNum = 1071;
		
		HederaContract contract = new HederaContract(shardNum, realmNum, contractNum);
		contract.txQueryDefaults = txQueryDefaults;

		
		final String SC_SET_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
		long gas = 250000;
		long amount = 14;
		byte[] functionParameters = SoliditySupport.encodeSet(10,SC_SET_ABI);
		
		ContractCall.call(contract, gas, amount, functionParameters);
		Thread.sleep(15000);
	}
}