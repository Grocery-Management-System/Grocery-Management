package grocery.system.controller;

import grocery.system.view.HomePage;
import grocery.system.view.ProductPage;
import grocery.system.view.DefineProductPage;
import grocery.system.view.StoragePage;



public class HomePageController {

    public HomePageController() {}

    public void defineBtnAct(){
        //sho
        DefineProductPage defineProduct = new DefineProductPage();
        /* show will be used once we implement scene for the pages
        defineProduct.show();
         */
    }

    public void productBtnAct(){
        ProductPage productPage = new ProductPage();
        /*
        productPage.show();
         */

    }

    public void storageBtnAct(){
        StoragePage storagePage = new StoragePage();
        /*
        storagePage.show();
         */
    }


}
