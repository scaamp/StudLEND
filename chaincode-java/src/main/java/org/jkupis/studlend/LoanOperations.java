/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jkupis.studlend;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "StudLEND",
                description = "Student lending system",
                version = "0.0.1-SNAPSHOT"))

@Default
public final class LoanOperations implements ContractInterface {

    private final Genson genson = new Genson();

    private enum LoanTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    /**
     * Creates some initial loans on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        createLoan(ctx, "1", "2", null, 2000, 30, 5);
        createLoan(ctx, "2", "1", null, 3000, 15, 3);
        createLoan(ctx, "3", "3", null, 500, 10, 6);
    }

    /**
     * Creates a new loan on the ledger.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Loan createLoan(final Context ctx, final String loanID, final String borrowerID, final String lenderID, final int amount,
        final int days, final double percent) {
        ChaincodeStub stub = ctx.getStub();

        if (loanExists(ctx, loanID)) {
            String errorMessage = String.format("Loan %s already exists", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Loan loan = new Loan(loanID, borrowerID, lenderID, amount, days, percent);
        //Use Genson to convert the Loan into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(loan);
        stub.putStringState(loanID, sortedJson);

        return loan;
    }

    /**
     * Retrieves an loan with the specified ID from the ledger.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Loan readLoan(final Context ctx, final String loanID) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);

        if (loanJSON == null || loanJSON.isEmpty()) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Loan loan = genson.deserialize(loanJSON, Loan.class);
        return loan;
    }

    /**
     * Deletes loan on the ledger.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteLoan(final Context ctx, final String loanID) {
        ChaincodeStub stub = ctx.getStub();

        if (!loanExists(ctx, loanID)) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(loanID);
    }

    /**
     * Checks the existence of the loan on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean loanExists(final Context ctx, final String loanID) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);

        return (loanJSON != null && !loanJSON.isEmpty());
    }

    /**
     * Changes the owner of a loan on the ledger.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String giveLoan(final Context ctx, final String loanID, final String lender, final int amount) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);

        if (loanJSON == null || loanJSON.isEmpty()) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Loan loan = genson.deserialize(loanJSON, Loan.class);

        Loan newLoan = new Loan(loan.getLoanID(), loan.getBorrowerID(), lender, amount, loan.getDays(), loan.getPercent());
        //Use a Genson to conver the Loan into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newLoan);
        stub.putStringState(loanID, sortedJson);

        return lender;
    }

    /**
     * Return a loan to the lender and returned amount
     *
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String returnLoan(final Context ctx, final String loanID, final String lender) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);


        if (loanJSON == null || loanJSON.isEmpty()) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Loan loan = genson.deserialize(loanJSON, Loan.class);
        double amountWithPercent = Math.round((loan.getAmount() + (loan.getAmount() * loan.getDays() * (loan.getPercent() * 0.01)) / 365));
        Loan newLoan = new Loan(loan.getLoanID(), loan.getBorrowerID(), lender, amountWithPercent, loan.getDays(), loan.getPercent());
        //Use a Genson to conver the Loan into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newLoan);
        stub.putStringState(loanID, sortedJson);
        stub.delState(loanID);

        return "Returned amount: " + amountWithPercent;
    }

    /**
     * Retrieves all loans from the ledger.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllLoans(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Loan> queryResults = new ArrayList<Loan>();

        // To retrieve all loans from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'loan0', endKey = 'loan9' ,
        // then getStateByRange will retrieve loan with keys between loan0 (inclusive) and loan9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Loan loan = genson.deserialize(result.getStringValue(), Loan.class);
            System.out.println(loan);
            queryResults.add(loan);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
}
