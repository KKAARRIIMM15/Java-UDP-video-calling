# Project Idea
The idea is to implement video and audio live streaming that transfere video frames and audio frames from one device to other device over WAN (Wide Area Network)
# Network Protocol
The main reason beyond why UDP is chosen in this project that it does not wait for lost packets to be re-sent, so the stream keeps playing smoothly even if a few packets are missing.

1- Supports Multicasting: UDP allows one sender to send data to many users at the same time (multicasting).This makes it efficient for live events like sports streaming.

2- Lower Bandwidth Usage: UDP has less overhead compared to TCP because it does not require acknowledgments, handshakes, or retransmissions.

3- Works Better in Unstable Networks: In wireless or mobile networks, data packets may be lost due to signal drops or congestion.UDP simply skips lost packets, so the video/audio continues playing smoothly without pauses

## Network configuration
### the project needs to apply Port Forwarding on the DSL router firewall
Why Should You Use Port Forwarding in WAN Connections?

When you are connected to the internet (WAN – Wide Area Network), your devices are typically behind a router or firewall, which blocks incoming connections for security reasons. 
Port forwarding allows external devices (on the internet) to access specific services on your local network.


