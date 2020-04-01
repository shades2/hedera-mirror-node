package com.hedera.mirror.importer.parser.domain;

/*
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.google.protobuf.ByteString;

import com.hedera.mirror.importer.exception.ParserException;
import com.hedera.mirror.importer.parser.record.transactionhandler.AbstractTransactionHandlerTest;

import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordItemTest extends AbstractTransactionHandlerTest {

    private static final Transaction DEFAULT_TRANSACTION = Transaction.newBuilder()
            .setBodyBytes(TransactionBody.getDefaultInstance().toByteString())
            .build();
    private static final byte[] DEFAULT_TRANSACTION_BYTES = DEFAULT_TRANSACTION.toByteArray();
    private static final TransactionRecord DEFAULT_RECORD = TransactionRecord.getDefaultInstance();
    private static final byte[] DEFAULT_RECORD_BYTES = DEFAULT_RECORD.toByteArray();

    // 'body' and 'bodyBytes' feilds left empty
    private static final Transaction TRANSACTION = Transaction.newBuilder()
            .setSigMap(SignatureMap.newBuilder()
                    .addSigPair(SignaturePair.newBuilder()
                            .setEd25519(ByteString.copyFromUtf8("ed25519"))
                            .setPubKeyPrefix(ByteString.copyFromUtf8("pubKeyPrefix"))
                            .build())
                    .build())
            .build();

    private static final TransactionBody TRANSACTION_BODY = TransactionBody.newBuilder()
            .setTransactionFee(10L)
            .setMemo("memo")
            .build();

    private static final TransactionRecord TRANSACTION_RECORD = TransactionRecord.newBuilder()
            .setReceipt(TransactionReceipt.newBuilder().setStatusValue(22).build())
            .setMemo("memo")
            .build();

    @Test
    public void testBadTransactionBytesThrowException() {
        testException(new byte[] {0x0, 0x1}, DEFAULT_RECORD_BYTES, RecordItem.BAD_TRANSACTION_BYTES_MESSAGE);
    }

    @Test
    public void testBadRecordBytesThrowException() {
        testException(DEFAULT_TRANSACTION_BYTES, new byte[] {0x0, 0x1}, RecordItem.BAD_RECORD_BYTES_MESSAGE);
    }

    @Test
    public void testTransactionBytesWithoutTransactionBodyThrowException() {
        testException(Transaction.newBuilder().build().toByteArray(),
                DEFAULT_RECORD_BYTES, RecordItem.BAD_TRANSACTION_BODY_BYTES_MESSAGE);
    }

    @Test
    public void testWithBody() {
        Transaction transaction = TRANSACTION.toBuilder().setBody(TRANSACTION_BODY).build();
        RecordItem recordItem = new RecordItem(transaction.toByteArray(), TRANSACTION_RECORD.toByteArray());
        assertRecordItem(transaction, recordItem);
    }

    @Test
    public void testWithBodyBytes() {
        Transaction transaction = TRANSACTION.toBuilder().setBodyBytes(TRANSACTION_BODY.toByteString()).build();
        RecordItem recordItem = new RecordItem(transaction.toByteArray(), TRANSACTION_RECORD.toByteArray());
        assertRecordItem(transaction, recordItem);
    }

    private void testException(byte[] transactionBytes, byte[] recordBytes, String expectedMessage) {
        Exception exception = assertThrows(ParserException.class, () -> {
            new RecordItem(transactionBytes, recordBytes);
        });
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertRecordItem(Transaction transaction, RecordItem recordItem) {
        assertEquals(transaction, recordItem.getTransaction());
        assertEquals(TRANSACTION_RECORD, recordItem.getRecord());
        assertEquals(TRANSACTION_BODY, recordItem.getTransactionBody());
        assertArrayEquals(transaction.toByteArray(), recordItem.getTransactionBytes());
        assertArrayEquals(TRANSACTION_RECORD.toByteArray(), recordItem.getRecordBytes());
    }
}
