/**
 *
 */
package hu.herba.util.codie.model;

/**
 * @author csorbazoli
 */
// Codie communicates with a custom binary protocol. All fields and data is little-endian.
// Little-endian means for example: 18 = 0x12, but on two bytes it will be 0x12 0x00 (so the first byte is the low, and
// the second is the high one!)
//
// The communication takes place among three nodes:
// App: The application which can be a mobile app on a mobile platform, a PC program� anything which is in the
// central BLE role, connecting to Codie.
// BLE: The BLE module inside Codie. There are special commands that can be executed only by the BLE module.
// MCU: The host microcontroller unit in Codie, executing the majority of the commands.
//
// The packet structure is as follows:
// INFO[8] SEQ[16] CMD[16] ARGLEN[16] ARGDAT[n]
// field purpose
// INFO - Packet information. (see details later)
// SEQ - Sequence number. A 16-bit packet counter. Used for sorting and acknowledge/reply identification.
// CMD - Command ID. 16-bit command ID.
// ARGLEN - Argument length. For each command, there can be one argument starting with a 16bit length field.
// ARGDAT - Argument data. Data accompanying the command (command specific structure).

// INFO: ROUTE + PRIO
// ROUTE: DESTINATION + SENDER
// 00 = APP, 01 = MCU, 10 = BLE. e.g. APP -> MCU = 0100 = 0x4, APP -> BLE = 1000 = 0x8
// PRIO: The four PRIO bits specify the priority of the packet. Currently only the most significant bit (P3) is
// used:
// if P3=0, the packet gets queued in the normal queue, if P3=1, the packet goes to the priority queue. Packets in
// the priority queue are executed first.
// E.g. normal prio = 0x0, high prio = 0x8

// SEQ The packet sequence number is a 16bit unsigned integer.
// Currently this seq number is not used in any way except in the replies (so a reply can be assigned to its
// original command).

// ARG The argument starts with the 16bit ARGLEN argument length in bytes, followed by the argument data. The data
// structure is command specific.

// Each command has a 16bit ID. The commands are grouped to four groups:
// General commands, which can be executed by any of the nodes.
// MCU commands can be executed only on the MCU.
// BLE commands can be executed only on the BLE module.
// App commands can be executed by the client application.
public interface CodieCommandBase {
	int FROM_APP = 0x00;
	int FROM_MCU = 0x10;
	int FROM_BLE = 0x20;
	int TO_APP = 0x00;
	int TO_MCU = 0x40;
	int TO_BLE = 0x80;

	int NORMAL = 0x00;
	int HIGH = 0x08;

	public static final String SEPARATOR = "|";

	/**
	 * @return Type of the command, where {@link CodieCommandType#name()} specifies the name of the command how it is triggered by Scratch, and
	 *         {@link CodieCommandType#getCommandId()} specifies the two byte integer that is known by Codie.
	 */
	CodieCommandType getCommandType();

	/**
	 * @return sender byte
	 */
	CodieRole getSender();

	/**
	 * @return destination byte
	 */
	CodieRole getDestination();
}
