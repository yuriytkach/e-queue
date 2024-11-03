var countdownInterval;

// Function to fetch booking status
function fetchBookingStatus(hasBooked = false) {
    $.get("/api/booking/status", function (data) {
        // Calculate time difference between server and client
        var serverTime = data.serverTime;
        var clientTime = Math.floor(Date.now() / 1000) % 86400; // Seconds since midnight
        var timeOffset = serverTime - clientTime;

        console.log('Booking status: Server time: ' + serverTime
            + ', Client time: ' + clientTime + ', Offset: ' + timeOffset);

        updateUIBasedOnBookingStatus(data, timeOffset, hasBooked);
    });
}

// Function to update the UI based on booking status
function updateUIBasedOnBookingStatus(data, timeOffset, hasBooked) {
    var bookingOpen = data.bookingOpen;
    var secondsLeft = data.secondsLeftInCurrentPhase;

    // Clear any existing intervals
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }

    $.get("/api/booking/winner", function (winnerData) {
        if (winnerData.slotId) {
            console.log('Winner: Slot ' + winnerData.slotId + ' has been won');
            displayWinnerMessage(winnerData.slotId);
        } else {
            if (bookingOpen) {
                console.log('Status: Booking is open');
                if (!hasBooked) {
                    console.log('Status: User has not booked yet');
                    loadBookingSlots();
                    $('#booking-form').show();
                    $('#booking-message').hide();
                }
                $('#booking-countdown').html('Booking ends in: ' + formatTime(secondsLeft));

                // Start countdown
                startCountdown(secondsLeft, 'Booking ends in: ', function () {
                    // After countdown ends, fetch status again
                    fetchBookingStatus();
                });
            } else {
                console.log('Status: Booking is closed');
                $('#booking-form').hide();
                $('#booking-countdown').html('Booking starts in: ' + formatTime(secondsLeft));
                $('#booking-message').show().text('Booking is currently closed. Please wait.');

                // Start countdown
                startCountdown(secondsLeft, 'Booking starts in: ', function () {
                    // After countdown ends, fetch status and reload slots
                    fetchBookingStatus();
                });
            }
        }
    });
}

// Function to start the countdown
function startCountdown(secondsLeft, messagePrefix, onComplete) {
    var remainingSeconds = secondsLeft;

    countdownInterval = setInterval(function () {
        remainingSeconds--;
        $('#booking-countdown').html(messagePrefix + formatTime(remainingSeconds));

        if (remainingSeconds <= 0) {
            clearInterval(countdownInterval);
            onComplete();
        }
    }, 1000);
}

// Function to format time in mm:ss
function formatTime(seconds) {
    var minutes = Math.floor(seconds / 60);
    var secs = seconds % 60;
    return ('0' + minutes).slice(-2) + ':' + ('0' + secs).slice(-2);
}

// Load booking slots into the dropdown
function loadBookingSlots() {
    $.get("/api/booking/slots", function (data) {
        var $slotSelect = $('#booking-slot');
        $slotSelect.empty();

        console.log('Loading booking slots: ' + data.length);
        if (data.length > 0) {
            data.forEach(function (slot) {
                $slotSelect.append('<option value="' + slot.id + '">' + slot.startTime + ' - ' + slot.endTime + '</option>');
            });
            $('#no-slots-message').hide();
            $('#booking-form').show();
        } else {
            // No slots available
            $('#booking-form').hide();
            $('#no-slots-message').show().text('No slots available. Please try next day.');
        }
    });
}

// Fetch user info and update UI
$.get("/user", function (data) {
    $("#user").html('Logged in as: ' + data.name);
    $(".unauthenticated").hide();
    $(".authenticated").show();

    checkIfWinner(); // Check if the user is a winner
});

function checkIfWinner() {
    $.get("/api/booking/winner", function (data) {
        if (data.slotId) {
            console.log('CheckWinner: User is a winner: ' + data.slotId);
            displayWinnerMessage(data.slotId);
        } else {
            console.log('CheckWinner: User is not a winner');
            checkIfAlreadyBooked();
        }
    });
}

function displayWinnerMessage(slotId) {
    $('#booking-form').hide();
    $('#booking-countdown').hide();
    $('#booking-message').show().text('Congratulations! You have won slot ' + slotId + '.');
}

function logout() {
    $.post("/logout", function () {
        $("#user").html('');
        $(".authenticated").hide();
        $(".unauthenticated").show();
        // Clear booking slots
        $('#booking-slot').empty();
        if (countdownInterval) {
            clearInterval(countdownInterval);
        }
    });
    return false;
}

function submitBooking() {
    var selectedSlotId = $('#booking-slot').val();
    $.post("/api/booking/book", {slotId: selectedSlotId}, function (response) {
        // Disable the booking form since the user has booked
        $('#booking-form').hide();
        $('#booking-message').show().text('You have already booked a slot in this window.');
    }).fail(function (xhr) {
        if (xhr.status === 403) {
            // Hide the booking form
            $('#booking-form').hide();
            $('#booking-message').show().text('You have already booked a slot in this window.');
        } else {
            alert('Booking failed. Please try again.');
        }
    });
}

function checkIfAlreadyBooked() {
    $.get("/api/booking/hasBooked", function (data) {
        if (data.hasBooked) {
            console.log('hasBooked: User has already booked a slot');
            // User has already booked in this window
            $('#booking-form').hide();
            $('#booking-message').show().text('You have already booked a slot in this window.');
        } else {
            console.log('hasBooked: User has not booked yet');
        }
        fetchBookingStatus(data.hasBooked);
    });
}
