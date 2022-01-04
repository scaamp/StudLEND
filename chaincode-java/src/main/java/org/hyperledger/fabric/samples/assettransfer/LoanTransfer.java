/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Loan Transfer",
                description = "The hyperlegendary loan transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class LoanTransfer implements ContractInterface {

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
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        CreateLoan(ctx, "1", "2", 2000, 30, 5);
        CreateLoan(ctx, "2", "1", 3000, 15, 3);
        CreateLoan(ctx, "3", "3", 500, 10, 6);
//        CreateLoan(ctx, "loan2", "red", 5, "Brad", 400);
//        CreateLoan(ctx, "loan3", "green", 10, "Jin Soo", 500);
//        CreateLoan(ctx, "loan4", "yellow", 10, "Max", 600);
//        CreateLoan(ctx, "loan5", "black", 15, "Adrian", 700);
//        CreateLoan(ctx, "loan6", "white", 15, "Michel", 700);

    }

    /**
     * Creates a new loan on the ledger.
     *
     * @param ctx the transaction context
     * @param loanID the ID of the new loan
//     * @param color the color of the new loan
//     * @param size the size for the new loan
//     * @param owner the owner of the new loan
//     * @param appraisedValue the appraisedValue of the new loan
     * @return the created loan
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Loan CreateLoan(final Context ctx, final String loanID, final String borrowerID, final int amount,
        final int days, final double percent) {
        ChaincodeStub stub = ctx.getStub();

        if (LoanExists(ctx, loanID)) {
            String errorMessage = String.format("Loan %s already exists", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Loan loan = new Loan(loanID, borrowerID, amount, days, percent);
        //Use Genson to convert the Loan into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(loan);
        stub.putStringState(loanID, sortedJson);

        return loan;
    }

    /**
     * Retrieves an loan with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param loanID the ID of the loan
     * @return the loan found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Loan ReadLoan(final Context ctx, final String loanID) {
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
     * Updates the properties of an loan on the ledger.
     *
     * @param ctx the transaction context
     * @param loanID the ID of the loan being updated
     * @param color the color of the loan being updated
     * @param size the size of the loan being updated
     * @param owner the owner of the loan being updated
     * @param appraisedValue the appraisedValue of the loan being updated
     * @return the transferred loan
     */
//    @Transaction(intent = Transaction.TYPE.SUBMIT)
//    public Loan UpdateLoan(final Context ctx, final String loanID, final String color, final int size,
//        final String owner, final int appraisedValue) {
//        ChaincodeStub stub = ctx.getStub();
//
//        if (!LoanExists(ctx, loanID)) {
//            String errorMessage = String.format("Loan %s does not exist", loanID);
//            System.out.println(errorMessage);
//            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
//        }
//
//        Loan newLoan = new Loan(loanID, color, size, owner, appraisedValue);
//        //Use Genson to convert the Loan into string, sort it alphabetically and serialize it into a json string
//        String sortedJson = genson.serialize(newLoan);
//        stub.putStringState(loanID, sortedJson);
//        return newLoan;
//    }

    /**
     * Deletes loan on the ledger.
     *
     * @param ctx the transaction context
     * @param loanID the ID of the loan being deleted
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteLoan(final Context ctx, final String loanID) {
        ChaincodeStub stub = ctx.getStub();

        if (!LoanExists(ctx, loanID)) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(loanID);
    }

    /**
     * Checks the existence of the loan on the ledger
     *
     * @param ctx the transaction context
     * @param loanID the ID of the loan
     * @return boolean indicating the existence of the loan
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean LoanExists(final Context ctx, final String loanID) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);

        return (loanJSON != null && !loanJSON.isEmpty());
    }

    /**
     * Changes the owner of a loan on the ledger.
     *
     * @param ctx the transaction context
     * @param loanID the ID of the loan being transferred
//     * @param newOwner the new owner
     * @return the old owner
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferLoan(final Context ctx, final String loanID, final String lender) {
        ChaincodeStub stub = ctx.getStub();
        String loanJSON = stub.getStringState(loanID);

        if (loanJSON == null || loanJSON.isEmpty()) {
            String errorMessage = String.format("Loan %s does not exist", loanID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, LoanTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Loan loan = genson.deserialize(loanJSON, Loan.class);

        Loan newLoan = new Loan(loan.getLoanID(), loan.getBorrowerID(), lender, loan.getAmount(), loan.getDays(), loan.getPercent());
        //Use a Genson to conver the Loan into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newLoan);
        stub.putStringState(loanID, sortedJson);

        return lender;
    }

    /**
     * Retrieves all loans from the ledger.
     *
     * @param ctx the transaction context
     * @return array of loans found on the ledger
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
