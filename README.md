force-cq
========

A AEM/CQ 5.5 bundle leveraging https://github.com/francoisledroff/force-rest-api

Give a try:

* upload with vault the content you can find under `src/main/resources` into your cq5.5 instance
* build and deploy the above bundle using maven
  * make sure you can download the cq5.5 dependencies in your maven local repositories
* get hold of a salesforce instance and configure a remote app with oAuth2 scope, with the refresh token option
* configure the ApiAuthServiceImpl OSGI service with
 * loginHost: the salesforce login host
 * consumerKey: the consumer key you got from the salesforce admin (see the previous step)
 * secretKey: the secret key you got from the salesforce admin (see the previous step)
 * redirect_uri: matching your server `https://yourserver/content/force/api/cqforce/apiauth.json`
* use our test drive page deployed in step 1: by browsing `https://yourserver/content/force/api/test-drive.html`

Enjoy and give us feedback.

