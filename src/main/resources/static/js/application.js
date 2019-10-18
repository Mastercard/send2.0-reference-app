$(document).ready(function() {
    // form
    $(".non-card-details").hide();
    $("#recipient-scheme").change(function () {
        var uriScheme = $(this).val();
        if (uriScheme != "PAN") {
            $("#recipient-card-details").hide();
            $("#recipient-expiry-year").removeAttr("required");
            $("#recipient-expiry-month").removeAttr("required");
            $("#recipient-name-on-account").attr("required", "");
            $("#recipient-non-card-details").show();
        } else {
            $("#recipient-non-card-details").hide();
            $("#recipient-name-on-account").removeAttr("required");
            $("#recipient-expiry-year").attr("required", "");
            $("#recipient-expiry-month").attr("required", "");
            $("#recipient-card-details").show();
        }
    })

    // alert success
    $(".json").hide();
    $("#show-request").click(function() {
        $(".json").slideDown("1000");
        $("#response").hide();
        $("#request").show();
    })

    $("#show-response").click(function() {
        $(".json").slideDown("1000");
        $("#request").hide();
        $("#response").show();
    })

    $("#reload").click(function(){
        location.reload(true);
    })

    $("#close").click(function() {
        $(".json").slideUp("1000");
    })
});