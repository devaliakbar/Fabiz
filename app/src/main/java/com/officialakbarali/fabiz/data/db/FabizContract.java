package com.officialakbarali.fabiz.data.db;

import android.provider.BaseColumns;

public class FabizContract {

    public static final class SyncLog implements BaseColumns {
        public static final String TABLE_NAME = "tb_sync_log";

        public static final String COLUMN_ROW_ID = TABLE_NAME + "_" + "row_id";
        public static final String COLUMN_TABLE_NAME = TABLE_NAME + "_" + "table_name";
        public static final String COLUMN_OPERATION = TABLE_NAME + "_" + "operation";
        public static final String COLUMN_OP_CODE = TABLE_NAME + "_" + "op_code";
        public static final String COLUMN_TIMESTAMP = TABLE_NAME + "_" + "time_stamp";
    }


    public static final class Info implements BaseColumns {
        public static final String TABLE_NAME = "tb_info";

        public static final String COLUMN_ORG_NAME = TABLE_NAME + "_" + "org_name";
        public static final String COLUMN_ADDRESS1 = TABLE_NAME + "_" + "address1";
        public static final String COLUMN_ADDRESS2 = TABLE_NAME + "_" + "address1";
        public static final String COLUMN_ADDRESS3 = TABLE_NAME + "_" + "address3";
        public static final String COLUMN_PHONE = TABLE_NAME + "_" + "phone";
        public static final String COLUMN_EMAIL = TABLE_NAME + "_" + "email";
        public static final String COLUMN_VAT_NO = TABLE_NAME + "_" + "vat_no";
    }


    public static final class PriceLevel implements BaseColumns {
        public static final String TABLE_NAME = "tb_price_level";

        public static final String COLUMN_PRICE_LEVEL_NAME = TABLE_NAME + "_" + "price_level_name";

    }


    public static final class Item implements BaseColumns {
        public static final String TABLE_NAME = "tb_item";

        public static final String COLUMN_NAME = TABLE_NAME + "_" + "name";
        public static final String COLUMN_BRAND = TABLE_NAME + "_" + "brand";
        public static final String COLUMN_CATEGORY = TABLE_NAME + "_" + "category";
        public static final String COLUMN_BARCODE = TABLE_NAME + "_" + "barcode";
        public static final String COLUMN_VAT_PERCENTAGE = TABLE_NAME + "_" + "vat_percentage";
        public static final String COLUMN_VAT_INCLUSIVE = TABLE_NAME + "_" + "vat_inclusive";
        public static final String COLUMN_COST = TABLE_NAME + "_" + "cost";
        public static final String COLUMN_STATUS = TABLE_NAME + "_" + "status";
        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_NAME = TABLE_NAME + "." + COLUMN_NAME;
        public static final String FULL_COLUMN_BRAND = TABLE_NAME + "." + COLUMN_BRAND;
        public static final String FULL_COLUMN_CATEGORY = TABLE_NAME + "." + COLUMN_CATEGORY;
        public static final String FULL_COLUMN_BARCODE = TABLE_NAME + "." + COLUMN_BARCODE;
        public static final String FULL_COLUMN_VAT_PERCENTAGE = TABLE_NAME + "." + COLUMN_VAT_PERCENTAGE;
        public static final String FULL_COLUMN_VAT_INCLUSIVE = TABLE_NAME + "." + COLUMN_VAT_INCLUSIVE;
        public static final String FULL_COLUMN_COST = TABLE_NAME + "." + COLUMN_COST;
        public static final String FULL_COLUMN_STATUS = TABLE_NAME + "." + COLUMN_STATUS;
    }

    public static final class ItemUnit implements BaseColumns {
        public static final String TABLE_NAME = "tb_item_unit";

        public static final String COLUMN_UNIT_NAME = TABLE_NAME + "_" + "name";
        public static final String COLUMN_QTY = TABLE_NAME + "_" + "qty";
        public static final String COLUMN_PRICE = TABLE_NAME + "_" + "price";
        public static final String COLUMN_RETAIL_PRICE = TABLE_NAME + "_" + "retail_price";
        public static final String COLUMN_BASIC_PRICE = TABLE_NAME + "_" + "basic_price";
        public static final String COLUMN_RETAIL_BASIC_PRICE = TABLE_NAME + "_" + "retail_basic_price";
        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_UNIT_NAME = TABLE_NAME + "." + COLUMN_UNIT_NAME;
        public static final String FULL_COLUMN_QTY = TABLE_NAME + "." + COLUMN_QTY;
        public static final String FULL_COLUMN_PRICE = TABLE_NAME + "." + COLUMN_PRICE;
        public static final String FULL_COLUMN_RETAIL_PRICE = TABLE_NAME + "." + COLUMN_RETAIL_PRICE;
        public static final String FULL_COLUMN_BASIC_PRICE = TABLE_NAME + "." + COLUMN_BASIC_PRICE;
        public static final String FULL_COLUMN_RETAIL_BASIC_PRICE = TABLE_NAME + "." + COLUMN_RETAIL_BASIC_PRICE;

    }


    public static final class Customer implements BaseColumns {
        public static final String TABLE_NAME = "tb_customer";

        public static final String COLUMN_BARCODE = TABLE_NAME + "_" + "barcode";
        public static final String COLUMN_CR_NO = TABLE_NAME + "_" + "cr_no";
        public static final String COLUMN_SHOP_NAME = TABLE_NAME + "_" + "shop_name";
        public static final String COLUMN_CONTACT_NAME = TABLE_NAME + "_" + "contact_name";
        public static final String COLUMN_PHONE = TABLE_NAME + "_" + "phone";
        public static final String COLUMN_DAY = TABLE_NAME + "_" + "day";
        public static final String COLUMN_EMAIL = TABLE_NAME + "_" + "email";
        public static final String COLUMN_ADDRESS_AREA = TABLE_NAME + "_" + "address" + "_area";
        public static final String COLUMN_ADDRESS_ROAD = TABLE_NAME + "_" + "address" + "_road";
        public static final String COLUMN_ADDRESS_BLOCK = TABLE_NAME + "_" + "address" + "_block";
        public static final String COLUMN_ADDRESS_SHOP_NUM = TABLE_NAME + "_" + "address" + "_shop_num";
        public static final String COLUMN_VAT_NUMBER = TABLE_NAME + "_" + "vat_number";
        public static final String COLUMN_PRICE_LEVEL = TABLE_NAME + "_" + "price_level";
        public static final String COLUMN_STATUS_CODE = TABLE_NAME + "_" + "status_code";


        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;

        public static final String FULL_COLUMN_BARCODE = TABLE_NAME + "." + COLUMN_BARCODE;
        public static final String FULL_COLUMN_CR_NO = TABLE_NAME + "." + COLUMN_CR_NO;
        public static final String FULL_COLUMN_SHOP_NAME = TABLE_NAME + "." + COLUMN_SHOP_NAME;
        public static final String FULL_COLUMN_CONTACT_NAME = TABLE_NAME + "." + COLUMN_CONTACT_NAME;
        public static final String FULL_COLUMN_PHONE = TABLE_NAME + "." + COLUMN_PHONE;
        public static final String FULL_COLUMN_DAY = TABLE_NAME + "." + COLUMN_DAY;
        public static final String FULL_COLUMN_EMAIL = TABLE_NAME + "." + COLUMN_EMAIL;
        public static final String FULL_COLUMN_ADDRESS_AREA = TABLE_NAME + "." + COLUMN_ADDRESS_AREA;
        public static final String FULL_COLUMN_ADDRESS_ROAD = TABLE_NAME + "." + COLUMN_ADDRESS_ROAD;
        public static final String FULL_COLUMN_ADDRESS_BLOCK = TABLE_NAME + "." + COLUMN_ADDRESS_BLOCK;
        public static final String FULL_COLUMN_ADDRESS_SHOP_NUM = TABLE_NAME + "." + COLUMN_ADDRESS_SHOP_NUM;
        public static final String FULL_COLUMN_VAT_NUMBER = TABLE_NAME + "." + COLUMN_VAT_NUMBER;
        public static final String FULL_COLUMN_PRICE_LEVEL = TABLE_NAME + "." + COLUMN_PRICE_LEVEL;
        public static final String FULL_COLUMN_STATUS_CODE = TABLE_NAME + "." + COLUMN_STATUS_CODE;
    }


    public static final class PendingSales {
        public static final String TABLE_NAME = "tb_pending_sales";

        public static final String COLUMN_BRANCH_ID = TABLE_NAME + "_" + "branch_id";
        public static final String COLUMN_CUST_ID = TABLE_NAME + "_" + "cust_id";
        public static final String COLUMN_BILL_ID = TABLE_NAME + "_" + "bill_id";
        public static final String COLUMN_BILL_DATE = TABLE_NAME + "_" + "bill_date";

        public static final String COLUMN_TAXABLE = TABLE_NAME + "_" + "taxable";
        public static final String COLUMN_VAT = TABLE_NAME + "_" + "vat";
        public static final String COLUMN_GROSS = TABLE_NAME + "_" + "gross";
        public static final String COLUMN_BILL_DISCOUNT = TABLE_NAME + "_" + "bill_discount";
        public static final String COLUMN_BILL_NET_AMOUNT = TABLE_NAME + "_" + "net_amount";
        public static final String COLUMN_RECEIVED = TABLE_NAME + "_" + "received";
        public static final String COLUMN_RETURNED = TABLE_NAME + "_" + "returned";
        public static final String COLUMN_OTHER_ADJUST = TABLE_NAME + "_" + "other_adjust";
        public static final String COLUMN_DISCOUNT_LATER = TABLE_NAME + "_" + "discount_later";
        public static final String COLUMN_DUE = TABLE_NAME + "_" + "due";
        public static final String COLUMN_STATUS_CODE = TABLE_NAME + "_" + "status_code";
    }


    ///newwwwwwwwwwwwwwwwwwwww


    //-1 REQUEST ITEM
    public static final class RequestItem implements BaseColumns {
        public static final String TABLE_NAME = "tb_request_item";

        public static final String COLUMN_NAME = TABLE_NAME + "_" + "name";
        public static final String COLUMN_QTY = TABLE_NAME + "_" + "quantity";
    }


    //5 TABLE BILL DETAIL
    public static final class BillDetail implements BaseColumns {
        public static final String TABLE_NAME = "tb_bill_detail";

        public static final String COLUMN_DATE = TABLE_NAME + "_" + "dateofbill";
        public static final String COLUMN_CUST_ID = TABLE_NAME + "_" + "custid";
        public static final String COLUMN_QTY = TABLE_NAME + "_" + "qty";
        public static final String COLUMN_PRICE = TABLE_NAME + "_" + "price";

        public static final String COLUMN_RETURNED_TOTAL = TABLE_NAME + "_" + "returned_total";
        public static final String COLUMN_CURRENT_TOTAL = TABLE_NAME + "_" + "current_total";

        public static final String COLUMN_PAID = TABLE_NAME + "_" + "paid";

        public static final String COLUMN_DISCOUNT = TABLE_NAME + "_" + "discount";

        public static final String COLUMN_DUE = TABLE_NAME + "_" + "due";

        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_DATE = TABLE_NAME + "." + COLUMN_DATE;
        public static final String FULL_COLUMN_CUST_ID = TABLE_NAME + "." + COLUMN_CUST_ID;
        public static final String FULL_COLUMN_PRICE = TABLE_NAME + "." + COLUMN_PRICE;
        public static final String FULL_COLUMN_QTY = TABLE_NAME + "." + COLUMN_QTY;
        public static final String FULL_COLUMN_PAID = TABLE_NAME + "." + COLUMN_PAID;


        public static final String FULL_COLUMN_DISCOUNT = TABLE_NAME + "." + COLUMN_DISCOUNT;

        public static final String FULL_COLUMN_DUE = TABLE_NAME + "." + COLUMN_DUE;

        public static final String FULL_COLUMN_RETURNED_TOTAL = TABLE_NAME + "." + COLUMN_RETURNED_TOTAL;
        public static final String FULL_COLUMN_CURRENT_TOTAL = TABLE_NAME + "." + COLUMN_CURRENT_TOTAL;
    }


    //6 TABLE CART
    public static final class Cart implements BaseColumns {
        public static final String TABLE_NAME = "tb_cart";

        public static final String COLUMN_BILL_ID = TABLE_NAME + "_" + "billid";
        public static final String COLUMN_ITEM_ID = TABLE_NAME + "_" + "itemid";

        public static final String COLUMN_UNIT_ID = TABLE_NAME + "_" + "unit_id";

        public static final String COLUMN_NAME = TABLE_NAME + "_" + "name";
        public static final String COLUMN_BRAND = TABLE_NAME + "_" + "brand";
        public static final String COLUMN_CATEGORY = TABLE_NAME + "_" + "category";
        public static final String COLUMN_PRICE = TABLE_NAME + "_" + "price";
        public static final String COLUMN_QTY = TABLE_NAME + "_" + "qty";
        public static final String COLUMN_TOTAL = TABLE_NAME + "_" + "total";
        public static final String COLUMN_RETURN_QTY = TABLE_NAME + "_" + "returnqty";

        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_BILL_ID = TABLE_NAME + "." + COLUMN_BILL_ID;
        public static final String FULL_COLUMN_ITEM_ID = TABLE_NAME + "." + COLUMN_ITEM_ID;

        public static final String FULL_COLUMN_UNIT_ID = TABLE_NAME + "." + COLUMN_UNIT_ID;

        public static final String FULL_COLUMN_NAME = TABLE_NAME + "." + COLUMN_NAME;
        public static final String FULL_COLUMN_BRAND = TABLE_NAME + "." + COLUMN_BRAND;
        public static final String FULL_COLUMN_CATAGORY = TABLE_NAME + "." + COLUMN_CATEGORY;
        public static final String FULL_COLUMN_PRICE = TABLE_NAME + "." + COLUMN_PRICE;
        public static final String FULL_COLUMN_QTY = TABLE_NAME + "." + COLUMN_QTY;
        public static final String FULL_COLUMN_TOTAL = TABLE_NAME + "." + COLUMN_TOTAL;
        public static final String FULL_COLUMN_RETURN_QTY = TABLE_NAME + "." + COLUMN_RETURN_QTY;
    }

    //7 TABLE SALES RETURN
    public static final class SalesReturn implements BaseColumns {
        public static final String TABLE_NAME = "tb_sales_return";

        public static final String COLUMN_DATE = TABLE_NAME + "_" + "dateofreturn";
        public static final String COLUMN_BILL_ID = TABLE_NAME + "_" + "billid";
        public static final String COLUMN_ITEM_ID = TABLE_NAME + "_" + "itemid";

        public static final String COLUMN_UNIT_ID = TABLE_NAME + "_" + "unit_id";

        public static final String COLUMN_PRICE = TABLE_NAME + "_" + "price";
        public static final String COLUMN_QTY = TABLE_NAME + "_" + "qty";
        public static final String COLUMN_TOTAL = TABLE_NAME + "_" + "total";

        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_DATE = TABLE_NAME + "." + COLUMN_DATE;
        public static final String FULL_COLUMN_BILL_ID = TABLE_NAME + "." + COLUMN_BILL_ID;
        public static final String FULL_COLUMN_ITEM_ID = TABLE_NAME + "." + COLUMN_ITEM_ID;

        public static final String FULL_COLUMN_UNIT_ID = TABLE_NAME + "." + COLUMN_UNIT_ID;

        public static final String FULL_COLUMN_PRICE = TABLE_NAME + "." + COLUMN_PRICE;
        public static final String FULL_COLUMN_QTY = TABLE_NAME + "." + COLUMN_QTY;
        public static final String FULL_COLUMN_TOTAL = TABLE_NAME + "." + COLUMN_TOTAL;

    }

    //8 TABLE PAYMENT
    public static final class Payment implements BaseColumns {
        public static final String TABLE_NAME = "tb_payment";

        public static final String COLUMN_BILL_ID = TABLE_NAME + "_" + "billid";
        public static final String COLUMN_DATE = TABLE_NAME + "_" + "dateofreturn";
        public static final String COLUMN_AMOUNT = TABLE_NAME + "_" + "amount";

        public static final String COLUMN_TYPE = TABLE_NAME + "_" + "type";
        //FULL COLUMN NAME
        public static final String FULL_COLUMN_ID = TABLE_NAME + "." + _ID;
        public static final String FULL_COLUMN_BILL_ID = TABLE_NAME + "." + COLUMN_BILL_ID;
        public static final String FULL_COLUMN_DATE = TABLE_NAME + "." + COLUMN_DATE;
        public static final String FULL_COLUMN_AMOUNT = TABLE_NAME + "." + COLUMN_AMOUNT;

        public static final String FULL_COLUMN_TYPE = TABLE_NAME + "." + COLUMN_TYPE;
    }
}
