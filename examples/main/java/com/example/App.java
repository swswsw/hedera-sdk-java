package com.example;
    
    import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.account.AccountCreate;
import com.hedera.contracts.ContractCall;
import com.hedera.contracts.ContractCreate;
import com.hedera.contracts.ContractGetBytecode;
import com.hedera.contracts.ContractGetInfo;
import com.hedera.contracts.ContractRunLocal;
import com.hedera.contracts.ContractUpdate;
import com.hedera.contracts.DemoContract;
import com.hedera.contracts.SoliditySupport;
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

import fi.iki.elonen.NanoHTTPD;
    // NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
    //       instead of the above import use the following:
	// import org.nanohttpd.NanoHTTPD;
    
    public class App extends NanoHTTPD {
    	final static Logger logger = LoggerFactory.getLogger(NanoHTTPD.class);
    
        public App() throws IOException {
            super(8080);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }
    
        public static void main(String[] args) {
            try {
                new App();
            } catch (IOException ioe) {
                System.err.println("Couldn't start server:\n" + ioe);
            }
        }
    
        @Override
        public Response serve(IHTTPSession session) {
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> parms = session.getParms();
            if (parms.get("username") == null) {
                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
            } else {
                msg += "<p>Hello, " + parms.get("username") + "!</p>";
            }
            
//            try {
//            	demoContract();
//            } catch (Exception ex) {
//            	ex.printStackTrace();
//            }
            
            return newFixedLengthResponse(msg + "</body></html>\n");
        }
        
        private static void demoContract() throws Exception {
        	// setup a set of defaults for query and transactions
    		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
    		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

    		// create an account
    		// new account object
    		HederaAccount account = new HederaAccount();
    		// setup transaction/query defaults (durations, etc...)
    		account.txQueryDefaults = txQueryDefaults;

    		// create an account
    		HederaCryptoKeyPair newAccountKey = new HederaCryptoKeyPair(KeyType.ED25519);
    		account = AccountCreate.create(account, newAccountKey, 658720000l);
    		Thread.sleep(15000);
    		
    		if (account != null) {
    			// the paying account is now the new account
    			txQueryDefaults.payingAccountID = account.getHederaAccountID();
    			txQueryDefaults.payingKeyPair = newAccountKey;
    			txQueryDefaults.fileWacl = newAccountKey;

    			// create a file
    			// new file object
    			HederaFile file = new HederaFile();
    			// setup transaction/query defaults (durations, etc...)
    			file.txQueryDefaults = txQueryDefaults;

    			// get file contents
    			InputStream is = DemoContract.class.getResourceAsStream("/main/resources/simpleStorage.bin");
    		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    		    int nRead;
    		    byte[] data = new byte[4096];
    		    while ((nRead = is.read(data, 0, data.length)) != -1) {
    		        buffer.write(data, 0, nRead);
    		    }
    		 
    		    buffer.flush();
    		    byte[] fileContents = buffer.toByteArray();
    		    
    			// create a file with contents
    		    Thread.sleep(15000);
    			file = FileCreate.create(file, fileContents);
    			Thread.sleep(15000);

    			// new contract object
    			HederaContract contract = new HederaContract();
    			// setup transaction/query defaults (durations, etc...)
    			contract.txQueryDefaults = txQueryDefaults;

    			
    			// due to bug, we have to alternating between gas.
    			//long createGas = 5;
    			long createGas = 32936 * 2;
    			
    			// create a contract
    			Thread.sleep(15000);
    			contract = ContractCreate.create(contract, file.getFileID(), 0, createGas);
    			Thread.sleep(15000);
    			//contract = create(contract, file.getFileID(), 1);
    			if (contract != null) {
    				// update the contract
    				HederaTimeStamp expirationTime = new HederaTimeStamp(100, 10);
    				HederaDuration autoRenewDuration = new HederaDuration(10, 20);
    				
    				contract = ContractUpdate.update(contract, expirationTime, autoRenewDuration);
    				Thread.sleep(15000);
    				
    				if (contract != null) {
    					// getinfo
    					ContractGetInfo.getInfo(contract);
    					Thread.sleep(15000);
    					// get bytecode
    					ContractGetBytecode.getByteCode(contract);
    					Thread.sleep(15000);
    					// call
    					final String SC_SET_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
    					long gas = 250000;
    					long amount = 14;
    					byte[] functionParameters = SoliditySupport.encodeSet(10,SC_SET_ABI);
    					
    					ContractCall.call(contract, gas, amount, functionParameters);
    					Thread.sleep(15000);
    					// call local
    					String SC_GET_ABI = "{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
    					
    					byte[] function = SoliditySupport.encodeGetValue(SC_GET_ABI);
    					long localGas = 250000;
    					long maxResultSize = 5000;
    					HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, localGas, maxResultSize, function);
    					int decodeResult = SoliditySupport.decodeGetValueResult(functionResult.contractCallResult(),SC_GET_ABI);
    					logger.info(String.format("===>Decoded functionResult= %d", decodeResult));
    				}
    			}
    		}
        }
    }