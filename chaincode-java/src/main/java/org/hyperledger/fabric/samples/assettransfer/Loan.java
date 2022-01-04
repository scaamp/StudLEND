/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Loan {

    @Property()
    private final String loanID;

    @Property()
    private final String borrowerID;

    @Property()
    private final String lenderID;

    @Property
    private final int amount;

    @Property()
    private final int days;

    @Property()
    private final double percent;

    public String getLoanID() {
        return loanID;
    }

    public String getBorrowerID() {
        return borrowerID;
    }

    public String getLenderID() {
        return lenderID;
    }

    public int getAmount() {
        return amount;
    }

    public int getDays() {
        return days;
    }

    public double getPercent() {
        return percent;
    }

    public Loan(@JsonProperty("loanID") final String loanID, @JsonProperty("lenderID") final String lenderID,
            @JsonProperty("borrowerID") final String borrowerID, @JsonProperty("amount") final int amount,
            @JsonProperty("days") final int days, @JsonProperty("percent") final double percent) {
        this.loanID = loanID;
        this.lenderID = lenderID;
        this.borrowerID = borrowerID;
        this.amount = amount;
        this.days = days;
        this.percent = percent;
    }

    public Loan(@JsonProperty("loanID") final String loanID, @JsonProperty("borrowerID") final String borrowerID,
                @JsonProperty("amount") final int amount, @JsonProperty("days") final int days,
                @JsonProperty("percent") final double percent) {
        this.loanID = loanID;
        this.lenderID = null;
        this.borrowerID = borrowerID;
        this.amount = amount;
        this.days = days;
        this.percent = percent;
    }

//    @Override
//    public boolean equals(final Object obj) {
//        if (this == obj) {
//            return true;
//        }
//
//        if ((obj == null) || (getClass() != obj.getClass())) {
//            return false;
//        }
//
//        Loan other = (Loan) obj;
//
//        return Objects.deepEquals(
//                new String[] {getLoanID(), getColor(), getOwner()},
//                new String[] {other.getLoanID(), other.getColor(), other.getOwner()})
//                &&
//                Objects.deepEquals(
//                new int[] {getSize(), getAppraisedValue()},
//                new int[] {other.getSize(), other.getAppraisedValue()});
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getLoanID(), getColor(), getSize(), getOwner(), getAppraisedValue());
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [loanID=" + loanID + ", color="
//                + color + ", size=" + size + ", owner=" + owner + ", appraisedValue=" + appraisedValue + "]";
//    }
}
