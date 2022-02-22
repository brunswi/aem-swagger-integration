/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mysite.core.models;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.mysite.petstore.api.PetApi;
import com.mysite.petstore.invoker.ApiClient;
import com.mysite.petstore.invoker.ApiException;
import com.mysite.petstore.model.Pet;
import com.mysite.petstore.model.Pet.StatusEnum;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = Resource.class)
public class HelloWorldModel {

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;
    @SlingObject
    private ResourceResolver resourceResolver;

    private String message;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    protected void init() {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        String currentPagePath = Optional.ofNullable(pageManager)
                .map(pm -> pm.getContainingPage(currentResource))
                .map(Page::getPath).orElse("");

        message = "Hello World!\n"
            + "Resource type is: " + resourceType + "\n"
            + "Current page is:  " + currentPagePath + "\n";
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            ApiClient defaultClient = new ApiClient(httpClient);
            defaultClient.setBasePath("https://petstore3.swagger.io/api/v3");
            PetApi apiInstance = new PetApi(defaultClient);
            message += "\nCome visit our Pet Store!";
            // list available pets
            message += "\nCurrently in Stock:\n";
            message += listPets(apiInstance, StatusEnum.AVAILABLE);
            // list pending pets
            message += "\nPending:\n";
            message += listPets(apiInstance, StatusEnum.PENDING);
            // list sold pets
            message += "\nAlready Sold:\n";
            message += listPets(apiInstance, StatusEnum.SOLD);
        } catch (ApiException e) {
            message += "\nError while requesting pets: " + e.getMessage();
            logger.warn(e.getMessage(), e);
        }

    }

    private String listPets(PetApi apiInstance, StatusEnum status) throws ApiException {
        StringBuilder message = new StringBuilder();
        List<Pet> pets = apiInstance.findPetsByStatus(status.getValue());
        for(Pet pet: pets) {
            message.append(pet.getName()).append("\n");
        }
        return message.toString();
    }

    public String getMessage() {
        return message;
    }

}
