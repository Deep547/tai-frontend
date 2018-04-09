$(document).ready(function() {
	// Details/summary polyfill from frontend toolkit
	GOVUK.details.init()

	// Use GOV.UK shim-links-with-button-role.js to trigger a link styled to look like a button,
	// with role="button" when the space key is pressed.
	GOVUK.shimLinksWithButtonRole.init()

	var showHideContent = new GOVUK.ShowHideContent()
	showHideContent.init()

	// Move focus to error summary
	$('.error-summary').focus();



	// Character/Word Count
      var charCount = new GOVUK.CharCount()
      charCount.init({
        selector: 'js-char-count',
        highlight: true
      })

});

// re-enable any disabled buttons when navigating back
// Safari Mac/iOS
window.onpageshow = function(event) {
    $('[type="submit"][disabled]').removeAttr('disabled');
}

/* Back link configuration */
// store referrer value to cater for IE - https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/10474810/  */
var docReferrer = document.referrer
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
}
var backLinkElem = document.getElementById("backLink");
if (backLinkElem !=  null){
    if (window.history && window.history.back && typeof window.history.back === 'function') {
        var backScript = (docReferrer === "" || docReferrer.indexOf(window.location.host) !== -1) ? "javascript:window.history.back(); return false;" : "javascript:void(0);"
        backLinkElem.setAttribute("onclick",backScript);
        backLinkElem.setAttribute("href","javascript:void(0);");
    }
}

/* Temp - pending AF update to > 3.0.2 */
window.GOVUK.performance.sendGoogleAnalyticsEvent = function (category, event, label) {
  if (window.ga && typeof(window.ga) === 'function') {
    ga('send', 'event', category, event, label);
  } else {
    _gaq.push(['_trackEvent', category, event, label, undefined, true]);
  }
};