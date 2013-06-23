
module GCMService (sendGCM,GCMmessage(..),GCMresult(..),MRes(..)) where

import Send
import Types

data GCMServerSettings = GCMServerSettings{
        ApiKey :: String
    ,   ProjectId :: String
}

class PushNotificationService m message result | m -> message -> result where
    send :: m a -> message -> Int -> IO result

class GCMPushService m where
    send :: m a -> message -> Int -> IO result
    
instance PushNotificationService GCMmessage GCMresult where
    send = sendGCM
