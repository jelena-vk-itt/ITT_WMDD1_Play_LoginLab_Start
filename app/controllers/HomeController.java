package controllers;

import play.mvc.*;
import views.html.*;

import play.api.Environment;
import play.data.*;
import play.db.ebean.Transactional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import models.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    // Declare a private FormFactory instance
    private FormFactory formFactory;

    // Inject an instance of FormFactory into the controller via its constructor
    @Inject
    public HomeController(FormFactory f) {
        this.formFactory = f;
    }

    public Result index(Long cat) {
        
        // Declare the product list
        List<Product> productList = null;

        // Get the list of all categories in ascending order
        List<Category> categoryList = Category.findAll();

        if (cat == 0) {
            // Get list of all products
            productList = Product.findAll();
        } else {
            // Get products for selected category: find category first,
            // then extract products for that category
            productList = Category.find.ref(cat).getProducts();
        }
        
        return ok(index.render(productList, categoryList));
    }

    public Result addProduct() {

        Form<Product> productForm = formFactory.form(Product.class);

        return ok(addProduct.render(productForm));        
    }


    public Result addProductSubmit() {

        // Retrieve the submitted form object (bind from the HTTP request)
        Form<Product> newProductForm = formFactory.form(Product.class).bindFromRequest();

        // Check for errors (based on constraints set in the Product class)
        if (newProductForm.hasErrors()) {
            // Display the form again by returning a bad request
            return badRequest(addProduct.render(newProductForm));
        } else {
            // No errors found - extract the product detail from the form
            Product newProduct = newProductForm.get();
            
            // A new, unsaved, product will not have an id
            if (newProduct.getId() == null) {
                // Save the object to the Products table in the database
                newProduct.save();
                // Set a success message in flash (for display in return view)
                flash("success", "Product " + newProduct.getName() + " was added");
            } else if (newProduct.getId() != null) {
                // Product exists
                newProduct.update();
                // Set a success message in flash (for display in return view)
                flash("success", "Product " + newProduct.getName() + " was updated");
            }
            
            // Redirect to the index page
            return redirect(controllers.routes.HomeController.index(0));
        }
    }
  
    public Result deleteProduct(Long id) {
        
        // find product by id and call delete method
        Product.find.ref(id).delete();

        // Add message to flash session
        flash("success", "Product has been deleted");

        // Redirect to index page
        return redirect(routes.HomeController.index(0));
    }

    @Transactional
    public Result updateProduct(Long id) {

        Product p;
        Form<Product> productForm;

        try {
            // Find the product by id
            p = Product.find.byId(id);

            // Create a form based on the Product class and fill using p
            productForm = formFactory.form(Product.class).fill(p);

        } catch (Exception ex) {
            // Display an error message or page
            return badRequest("error");
        }

        // Render the updateProduct view - pass form as parameter
        return ok(addProduct.render(productForm));
    }
}

