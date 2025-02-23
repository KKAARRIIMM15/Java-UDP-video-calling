# Project Idea
The idea is to implement video and audio live streaming that transfere video frames and audio frames from one device to other device over WAN using JAVA Programming Language
# Network Protocol used in the project
The main reason beyond why UDP is chosen in this project that it does not wait for lost packets to be re-sent, so the stream keeps playing smoothly even if a few packets are missing.

1- Supports Multicasting: UDP allows one sender to send data to many users at the same time (multicasting).This makes it efficient for live events like sports streaming.

2- Lower Bandwidth Usage: UDP has less overhead compared to TCP because it does not require acknowledgments, handshakes, or retransmissions.

3- Works Better in Unstable Networks: In wireless or mobile networks, data packets may be lost due to signal drops or congestion.UDP simply skips lost packets, so the video/audio continues playing smoothly without pauses

## Network configuration applied in the project
### The project needs to apply Port Forwarding on the DSL router firewall
Why Should You Use Port Forwarding in WAN Connections?  
When you are connected to the internet (WAN – Wide Area Network), your devices are typically behind a router or firewall, which blocks incoming connections for security reasons. 
Port forwarding allows external devices (on the internet) to access specific services on your local network.


How Does Port Forwarding Work?  
1- Your router has a public IP address (WAN IP) that is visible on the internet.  
2- Your devices inside your network (LAN) have private IPs that are not directly accessible from the internet.  
3- Port forwarding tells your router to forward specific incoming traffic (from the internet) to a particular device inside your LAN.  


# Serialization
### Why Should Video Frames and Audio Frames Be Serialized Before Transmission Over a Network?
1- Computers store video and audio data as complex structures in memory (arrays, buffers, etc.).  
2- Networks do not understand these formats directly—they only transfer data as streams of bytes.  
3- Serialization converts frames into a standard byte format that can be sent over a network.  
4- Video and audio frames must be played in sequence to maintain synchronization. Serialization ensures frames are properly ordered and can be reconstructed in the correct order at the receiver.  
5- Video and audio frames need to be kept in sync so that speech matches the video. Serialization often includes timestamps to help synchronize audio with video.  



