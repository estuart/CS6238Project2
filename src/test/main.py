# coding=utf-8
import requests
import sys
import hashlib
import time

from OpenSSL import crypto


class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


class dtime:
    #Change to smaller numbers for easier testing (3000, 6000)
    THIRTY = 30000
    SIXTY = 60000
    SLEEP30 = 30
    SLEEP60 = 60

class users:
    CLIENT_0 = "evan.stuart@gtri.gatech.edu"
    CLIENT_1 = "michael.puckett@gtri.gatech.edu"
    CLIENT_2 = "bobs.burgers@gtri.gatech.edu"

class Client:
    # Root url of our API
    #BASE_URL = "https://52.22.45.83:8443/s2dr/"
    BASE_URL = "https://localhost:8443/s2dr/"

    URLS = {'login': BASE_URL + "login",
            'personal': BASE_URL + "personal",
            'upload': BASE_URL + "upload/",
            'download': BASE_URL + "document/",
            'logout': BASE_URL + "logout/",
            'delete': BASE_URL + "document/",
            'delegate': BASE_URL + "document/",
            'signature': "/signature"
            }

    def __init__(self, cert, key):
        self.session = requests.Session()
        # TODO Remove this once certs are figured out
        self.cert = cert
        self.key = key

    def __str__(self):
        return 'Client={0}, Cert={1}, Key={2}'.format(self.session, self.cert, self.key)

    def login(self):
        try:
            login_request = self.session.post(url=self.URLS['login'], cert=(self.cert, self.key), verify=False)
            return login_request
        except requests.RequestException as e:
            print e
            sys.exit(1)

# security flags is a comma delimited list of flags
    def upload(self, file_path, filename, security_flags):
        signature = self.sign(file_path)
        try:
            # Create a dict with the upload parameters as key value pairs
            _files = {'document': open(file_path, 'rb'),
                      "documentName": filename,
                      "securityFlags": security_flags,
                      "signature": signature}
            # I pass a files dict as a way to force request lib to send form-data instead of www-encoded-form data
            upload_request = self.session.post(url=self.URLS['upload'], files=_files, cert=(self.cert, self.key), verify=False)
            return upload_request
        except requests.RequestException as e:
            print e
            sys.exit(1)

    def download(self, document_id, filename):
        try:
            download_request = self.session.get(url=self.URLS['download'] + str(document_id), stream=True,
                                                cert=(self.cert, self.key),
                                                verify=False)

            if download_request.status_code == 200:
                try:
                    with open(str(filename), 'wb') as f:
                        for chunk in download_request:
                            f.write(chunk)
                except (IOError, requests.RequestException) as e:
                    print "I/O error({0}): {1}".format(e.errno, e.strerror)
            else:
                # print "status:" + str(download_request.status_code)
                pass
            return download_request

        except requests.RequestException as e:
            print e
            sys.exit(1)

    def delegate(self, document_id, userName, timeLimitMillis, canPropogate, *permissions):
        # TODO Implement delegate function
        perms = [x for x in permissions]

        data = {"permissions": permissions,
                "userName": userName,
                "timeLimitMillis": timeLimitMillis,
                "canPropogate": canPropogate
                }
        try:
            delegate_request = self.session.put(url=self.URLS['delegate'] + str(document_id),
                                                json=data,
                                                cert=(self.cert, self.key),
                                                verify=False)
            return delegate_request
        except requests.RequestException as e:
            print e
            sys.exit(1)
        pass

    def delete(self, document_id):
        try:
            delete_request = self.session.delete(url=self.URLS['delete'] + str(document_id), cert=(self.cert, self.key), verify=False)
            return delete_request
        except requests.RequestException as e:
            print e
            sys.exit(1)

    def sign(self, file_path):
        key = crypto.load_privatekey(crypto.FILETYPE_PEM, open(self.key).read())
        file_content = open(file_path).read()
        signature = crypto.sign(key, file_content, 'sha256')
        return signature

    def get_signature(self, document_id):
        try:
            signature_request = self.session.get(url=self.URLS['download'] + str(document_id) + self.URLS['signature'],
                                                 stream=True,
                                                 cert=(self.cert, self.key),
                                                 verify=False)
        except requests.RequestException as e:
            print e
            sys.exit(1)
        return signature_request

    def logout(self):
        try:
            logout_request = self.session.post(self.URLS['logout'], cert=(self.cert, self.key), verify=False)
            return logout_request
        except requests.RequestException as e:
            print e
            sys.exit(1)

# Place holders for the different test scenarios we have to run.


def printOut(test_num, status, message=""):
    if status is "ERROR":
        print bcolors.OKBLUE + "[Test " + str(test_num) + "] " + bcolors.ENDC + bcolors.FAIL + "[**ERROR**] " + bcolors.ENDC + message
    if status is "SUCCESS":
        print bcolors.OKBLUE + "[Test " + str(test_num) + "] " + bcolors.ENDC + bcolors.OKGREEN + "[SUCCESS]" + bcolors.ENDC
    if status is "FAILED":
        print bcolors.OKBLUE + "[Test " + str(test_num) + "] " + bcolors.ENDC + bcolors.FAIL + "[FAILED] " + bcolors.ENDC
    if status is "LOG":
        print bcolors.OKBLUE + "[Test " + str(test_num) + "] " + bcolors.ENDC + message
    if status is "HEADER":
        print bcolors.HEADER + "\n[Test " + str(test_num) + "]" + bcolors.ENDC
    if status is "SLEEP":
        print bcolors.OKBLUE + "[Test " + str(test_num) + "] " + bcolors.ENDC + bcolors.WARNING + message + bcolors.ENDC

def test1(client_0):
    """
    Test Case: Checking-in with INTEGRITY security flag
        -Initialize session as client_0
        -Check in document "0.txt" with INTEGRITY flag
        -Show where the checked-in document and its signature will be located on the server
        -Cant really show this here, will include in README
    :returns: checkin result
    """
    printOut(1, "HEADER")
    printOut(1, "LOG", "Attempting to initialize session for client_0")
    client_0.login()
    printOut(1, "LOG", "Session initialized as client_0")
    printOut(1, "LOG", "Attempting to check in document '0.txt' with INTEGRITY flag set")
    request = client_0.upload('files/0.txt', '0.txt', 'INTEGRITY')

    if request.status_code == 201:
        result = True
        printOut(1, "LOG", "Upload of '0.txt' is successful")
        return result, client_0
    else:
        result = False
        printOut(1, "ERROR", "Upload of '0.txt' failed")
        return result, client_0


def test2(client_0):
    """
    Test Case: Checking-out a document as its owner
        -Using the same session, check out the document we just uploaded
        -Store retrieved file as 0_copy.txt
    :returns checkout result
    """
    printOut(2, "HEADER")
    printOut(2, "LOG", "Attempting to download 0.txt as 0_copy.txt")
    request = client_0.download('0.txt', "files/0_copy.txt")
    if request.status_code is 200:
        printOut(2, "LOG", "Download of '0_copy.txt' successful")
        result = True
    else:
        printOut(2, "ERROR", "Download of '0_copy.txt' failed")
        result = False
    return result


def test3(client_1):
    """
    Test Case: Checking-out a document as a user without access permission
        -Initialize a session with the server as the second client (client_1) and check
         out the document "0.txt" and attempt to store it as "0_copy.txt"
        -This should fail
    :returns: checkout result
    """
    printOut(3, "HEADER")
    printOut(3, "log", "Attempting to initialize session for client_1")
    client_1.login()
    printOut(3, "LOG", "Session initialized as client_1")
    printOut(3, "LOG", "Attempting to download 0.txt as client_1")
    request = client_1.download("0.txt", "downloads_1/0_copy.txt")
    if request.status_code == 200:
        printOut(3, "LOG", "Download of 0.txt as 0_copy.txt successful")
        result = True
    else:
        printOut(3, "ERROR", "[Insufficient Permissions] Download of 0.txt failed")
        result = False

    return result


def test4(client_0):
    """
    Test Case: Safe Deletion
        -Using the first session, safely delete "0.txt"
        -Then attempt to check it out again and store as "0_copy2.txt
    :returns: delete result, checkout result
    """
    printOut(4, "HEADER")
    printOut(4, "LOG", "Attempting to delete '0.txt' as client_0")
    request = client_0.delete("0.txt")
    if request.status_code == 200:
        printOut(4, "LOG", "Secure deletion of '0.txt' successful")
    else:
        printOut(4, "ERROR", "Secure delection of '0.txt' failed")
        return False
    request = client_0.download("0.txt", "files/0_copy2.txt")
    if request.status_code == 200:
        printOut(4, "LOG", "Download of 0.txt is successful")
    else:
        printOut(4, "ERROR", "Download of 0.txt failed, status code: "+ str(request.status_code))
        return False
    return True


def test5(client_0):
    """
    Test Case: Checking-in and Checking-out with CONFIDENTIALITY security flag
        -Using the first session, check in a second document "1.txt" with CONFIDENTIALITY
        -In readme show where the server copy of "1.txt" is located (needs to be encrypted)
        -Using same session check out second document and store as "1_copy.txt"
        -Terminate the first session
    :returns: checkin result, checkout result, session termination result
    """
    printOut(5, "HEADER")
    printOut(5, "LOG", "Attempting to check in second document '1.txt' with CONFIDENTIALITY")
    client_0.upload('files/1.txt', '1.txt', 'CONFIDENTIALITY')
    printOut(5, "LOG", "Check in of '1.txt' was successful")
    printOut(5, "LOG", "Attempting to check out '1.txt' and store it as 1_copy.txt")
    request = client_0.download('1.txt', 'downloads_0/1_copy.txt')
    if request.status_code == 200:
        printOut(5, "LOG", "Download of '1.txt' as '1_copy.txt' successful")
    else:
        printOut(5, "ERROR", "Download of '1.txt' as 1_copy.txt' failed")
        return False
    printOut(5, "LOG", "Attempting to terminate the first session (Client_0)")
    request = client_0.logout()
    if request.status_code ==200:
        printOut(5, "LOG", "The first session (client_0) terminated successfully")
    else:
        printOut(5, "ERROR", "There was an error terminating the firs session (client_0)")
        return False
    # If we get to this point return true because we have successfully completed the test
    return True



def test6(client_0):
    """
    Test Case: Updating a Document
        -Restart the first session
        -Checkin the second document "1.txt" with CONFIDENTIALITY|INTEGRITY flag
        -Verify the signature of the encrypted copy
        -Checkout "1.txt" and store it as "1_copy2.txt"
    :returns: checkin result, verify result, checkout result
    """
    printOut(6, "HEADER")
    printOut(6, "LOG", "Attempting to restart first session")
    request = client_0.login()
    if request.status_code == 200:
        printOut(6, "LOG", "Restart of first session was successful")
    else:
        printOut(6, "ERROR", "Restart of first session (client_0) failed")
        return False
    printOut(6, "LOG", "Attempting to checkin second document '1.txt' with CONFIDENTIALITY|INTEGRITY flag")
    request = client_0.upload('files/1.txt', '1.txt', 'INTEGRITY, CONFIDENTIALITY')
    if request.status_code == 201:
        printOut(6, "LOG", "Check in of '1.txt' with INTEGRITY and CONFIDENTIALITY was successful")
    else:
        printOut(6, "ERROR", "Check in of '1.txt' failed.")
        return False
    printOut(6, "LOG", "Attempting to verify signature of encrypted copy")
    printOut(6, "LOG", "Fetching the signature of '1.txt' from the server")
    request = client_0.get_signature("1.txt")
    server_sig = hashlib.sha256(request.content).hexdigest()
    if request.status_code == 200:
        printOut(6, "LOG", "Signature received from the server: " + str(server_sig))
    else:
        printOut(6, "ERROR", "Unable to receive signature from server.")
        return False
    # Now sign locally and compare to server signature
    printOut(6, "LOG", "Now generating a signature of '1.txt' locally")
    local = client_0.sign("files/1.txt")
    local_sig = hashlib.sha256(local).hexdigest()
    printOut(6, "LOG", "Signature generated by the client: "+str(local_sig))
    if local_sig == server_sig:
        printOut(6, "LOG", "Signature received from the server matches the local generated signature")
    else:
        printOut(6, "ERROR", "The signatures do not match.")
        return False
    printOut(6, "LOG", "Attempting to check out '1.txt' and store it as 1_copy2.txt")
    request = client_0.download('1.txt', 'downloads_0/1_copy2.txt')
    if request.status_code == 200:
        printOut(6, "LOG", "Download of '1.txt' as '1_copy2.txt' successful")
    else:
        printOut(6, "ERROR", "Download of '1.txt' as 1_copy.txt' failed")
        return False
    return True


def test7(client_0, client_1, client_2):
    """
    Test Case: Checking-in & Checking-out delegation without propogation
        -Using the first session, delegate("1.txt", "client_1",30,checking-in|checking-out, false)
        -Using the second session(client 1) checkout "1.txt" and store it as "1_copy.txt"
        -Using the second session, check in a different file as "1.txt"
        -Using the second session, delegate("1.txt", "client_2", 30, checking-in|checking-out, false)
        -Initialize a session as the third client (client_2) checkout the document"1.txt" and store it as "1_copy.txt"
        -After the delegation expires, using the second session (client_1), check out "1.txt"
         and store it as "1_copy2.txt"
        -Using the first session, check out "1.txt" and store it as "1_copy3.txt"
    :return:
    """
    result = True
    printOut(7, "HEADER")

    printOut(7, "LOG", "Delegating from client_0 -> client_1 for 30 seconds: delegate('1.txt', 'client_1',30,checking-in|checking-out, false) using client_0")
    request = client_0.delegate('1.txt', users.CLIENT_1, dtime.THIRTY, 'false', 'READ', 'WRITE')
    if request.status_code == 200:
        printOut(7, "LOG", "Permissions were successfully delegated to client_1 from client_0")
    else:
        printOut(7, "ERROR", "Permission delegation failed!")
        result = False

    printOut(7, "LOG", "Using the second session (client 1) attempting to checkout '1.txt' and store it as '1_copy.txt'")
    request = client_1.download('1.txt', 'downloads_1/1_copy.txt')
    if request.status_code == 200:
        printOut(7, "LOG", "Client 1 successfully checked out '1.txt' as 1_copy.txt")
    else:
        printOut(7, "ERROR", "Client 1 was unable to checkout '1.txt' as 1_copy.txt'")
        result = False

    printOut(7, "LOG", "Using client_1 checking in a different file as '1.txt'")
    request = client_1.upload('files/not_1.txt', '1.txt', 'INTEGRITY')
    if request.status_code == 201:
        printOut(7, "LOG", "Client 1 successfully checked in 'not_1.txt' as 1.txt")
    else:
        printOut(7, "ERROR", "Client 1 was unable to checkin 'not_1.txt' as '1.txt'")
        result = False

    printOut(7, "LOG", "Delegating from client_1 -> client_2 for 30 seconds: delegate('1.txt', 'client_2', 30, checking-in|checking-out, false)")
    request = client_1.delegate('1.txt', users.CLIENT_2, dtime.THIRTY, 'false', 'READ', 'WRITE')
    if request.status_code == 200:
        printOut(7, "LOG", "Permissions were successfully delegated from client_1 -> client_2")
    else:
        printOut(7, "ERROR", "Permission delegation failed!")
        result = False

    printOut(7, "LOG", "Initializing a third sessions (client_2)")
    request = client_2.login()
    if request.status_code == 200:
        printOut(7, "LOG", "Initialization of third session (client_2) was successful")
    else:
        printOut(7, "ERROR", "Initialization of third session (client_2) failed")
        result = False

    printOut(7, "LOG", "Checking out '1.txt' as client_2 and storing it as 1_copy.txt")
    request = client_2.download('1.txt', 'downloads_2/1_copy.txt')
    if request.status_code == 200:
        printOut(7, "LOG", "Client 2 successfully checked out '1.txt' as 1_copy.txt")
    else:
        printOut(7, "ERROR", "Client 2 was unable to checkout '1.txt' as 1_copy.txt'")
        result = False

    printOut(7, "SLEEP", "NOW SLEEPING FOR 30 SECONDS WAITING FOR DELEGATION TO EXPIRE")
    time.sleep(dtime.SLEEP30)
    request = client_1.download('1.txt', 'downloads_1/1_copy2.txt')
    if request.status_code == 200:
        printOut(7, "LOG", "Client 1 successfully checked out '1.txt' as 1_copy2.txt")
    else:
        printOut(7, "ERROR", "Client 1 was unable to checkout '1.txt' as 1_copy2.txt'")
        result = False
    printOut(7, "LOG", "Using the first session client_0, we will check out '1.txt' and store it as '1_copy3.txt'")
    request = client_0.download('1.txt', 'downloads_0/1_copy3.txt')
    if request.status_code == 200:
        printOut(7, "LOG", "Client 0 successfully checked out '1.txt' as 1_copy3.txt")
    else:
        printOut(7, "ERROR", "Client 0 was unable to checkout '1.txt' as 1_copy3.txt'")
        result = False
    return result


def test8(client_0, client_1, client_2):
    """
    Test Case: Checking-out delegation with propogation
        -Using the first session, delegate(“1.txt”, “client_1”, 30, checking-out, True). The delegation timeout should
         be 30 seconds.
        -Using the second session, delegate(“1.txt”, “client_2”, 60, checking-out, False). The delegation
         timeout should be 60 seconds.
        -Using the third session (client_2), check out “1.txt” and store it as “1_copy2.txt”.
        -After the 30-second delegation made by the owner expires, using the third session (client_2),
         check out “1.txt” and store it as “1_copy3.txt”.
        -After the 60-second delegation made by the second client expires, using the third session (client_2),
         check out “1.txt” and store it as “1_copy4.txt”.
        -Terminate all sessions

    :return:
    """
    result = True
    printOut(8, "HEADER")
    printOut(8, "LOG", "Delegating from client_0 -> client_1 for 30 seconds:  delegate('1.txt', 'client_1', 30, checking-out, true)")
    request = client_0.delegate('1.txt', users.CLIENT_1, dtime.THIRTY, 'true', 'READ')
    if request.status_code == 200:
        printOut(8, "LOG", "Permissions were successfully delegated from client_0 -> client_1 for 30 seconds")
    else:
        printOut(8, "ERROR", "Permission delegation failed!")
        result = False
    printOut(8, "LOG", "Delegating from client_1 -> client_2 for 60 seconds:  delegate('1.txt', 'client_2', 60, checking-out, false)")
    request = client_1.delegate('1.txt', users.CLIENT_2, dtime.SIXTY, 'false', 'READ')
    if request.status_code == 200:
        printOut(8, "LOG", "Permissions were successfully delegated from client_1 -> client_2 for 60 seconds")
    else:
        printOut(8, "ERROR", "Permission delegation failed!")
        result = False
    printOut(8, "LOG", "Checking out '1.txt' as client_2 and storing it as 1_copy2.txt")
    request = client_2.download('1.txt', 'downloads_2/1_copy2.txt')
    if request.status_code == 200:
        printOut(8, "LOG", "Client 2 successfully checked out '1.txt' as 1_copy2.txt")
    else:
        printOut(8, "ERROR", "Client 2 was unable to checkout '1.txt' as 1_copy2.txt'")
        result = False
    printOut(8, "SLEEP", "NOW SLEEPING FOR 30 SECONDS WAITING FOR DELEGATION TO EXPIRE")
    time.sleep(dtime.SLEEP30)
    request = client_2.download('1.txt', 'downloads_2/1_copy3.txt')
    if request.status_code == 200:
        printOut(8, "LOG", "Client 2 successfully checked out '1.txt' as 1_copy3.txt")
    else:
        printOut(8, "ERROR", "Client 2 was unable to checkout '1.txt' as 1_copy3.txt'")
        result = False
    printOut(8, "SLEEP", "NOW SLEEPING FOR 60 SECONDS WAITING FOR DELEGATION TO EXPIRE")
    time.sleep(dtime.SLEEP30)
    request = client_2.download('1.txt', 'downloads_2/1_copy4.txt')
    if request.status_code == 200:
        printOut(8, "LOG", "Client 2 successfully checked out '1.txt' as 1_copy4.txt")
    else:
        printOut(8, "ERROR", "Client 2 was unable to checkout '1.txt' as 1_copy4.txt'")
        result = False
    return result

# This silences annoying SSL warning for using a self signed cert
requests.packages.urllib3.disable_warnings()

# Conducting test 1
client_0 = Client('certs/s2drClient0.cert.pem', 'keys/s2drClient0.key.pem')
test1_result, client_0 = test1(client_0)
if test1_result is True:
    printOut(1, "SUCCESS")
else:
    printOut(1, "FAILED")

# Conducting test 2
test2_result = test2(client_0)
if test2_result is True:
    printOut(2, "SUCCESS")
else:
    printOut(2, "FAILED")

# Conducting test 3
client_1 =Client('certs/s2drClient1.cert.pem', 'keys/s2drClient1.key.pem')
test3_result = test3(client_1)
if test3_result is True:
    printOut(3, "SUCCESS")
else:
    printOut(3, "FAILED")

# Conducting test 4
test4_result = test4(client_0)
if test4_result is True:
    printOut(4, "SUCCESS")
else:
    printOut(4, "FAILED")

# Conducting test 5
test5_result = test5(client_0)
if test5_result is True:
    printOut(5, "SUCCESS")
else:
    printOut(5, "FAILED")

# Conducting test 6
test6_result = test6(client_0)
if test5_result is True:
    printOut(6, "SUCCESS")
else:
    printOut(6, "FAILED")

# Conducting test 7
client_2 = Client('certs/s2drClient3.cert.pem', 'keys/s2drClient3.key.pem')
test7_result = test7(client_0, client_1, client_2)
if test7_result is True:
    printOut(7, "SUCCESS")
else:
    printOut(7, "FAILED")

# Conducting test 8
test8_result = test8(client_0, client_1, client_2)
if test8_result is True:
    printOut(8, "SUCCESS")
else:
    printOut(8, "FAILED")

printOut(8, "SLEEP", "Now closing all sessions!")

