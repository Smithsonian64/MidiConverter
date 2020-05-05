/**
 *
 *
 *
 * @author Michael Smith
 */

import java.io.File;
import java.io.FileWriter;
import java.lang.constant.DynamicConstantDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import javax.sound.midi.*;

public class Test {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static void main(String[] args) throws Exception {

        Scanner scan = new Scanner(System.in);

        ArrayList<ArrayList<Double>> times = new ArrayList<>();
        for(int i = 0; i < 2; i++) times.add(new ArrayList<>());

        ArrayList<ArrayList<Integer>> types = new ArrayList<>();
        for(int i = 0; i < 2; i++) types.add(new ArrayList<>());

        ArrayList<Double> allTimes = new ArrayList<>();
        ArrayList<Integer> allTypes = new ArrayList<>();
        /*
        System.out.println("Please input file name\ninclude path if not in source directory: ");
        String inputFile = scan.nextLine();
        System.out.println("Please input output file name\nThe file will be placed in source directory: ");
        String outputFile = scan.nextLine();*/
        String inputFile = "velocitytest.mid";
        String outputFile = "vtDef.txt";

        Sequence sequence = MidiSystem.getSequence(new File(inputFile));

        System.out.println("Num of tracks: " + sequence.getTracks().length);

        System.out.println("PPQ: " + sequence.getResolution());

        System.out.println("Division type: " + sequence.getDivisionType());

        float PPQ = sequence.getResolution();
        long tempo = 120;
        double currentTime;
        double lastTime = 0;
        //double timeBetweenQs =

        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if(message instanceof javax.sound.midi.MetaMessage) {
                    MetaMessage m = (MetaMessage) message;
                    if(m.getType() == 0x51) {
                        String[] hexValues = byteToHex(m.getMessage());
                        String mSPQ = "";
                        System.out.print("Meta message, set Tempo: " + Arrays.toString(hexValues));
                        for(int j = 3; j < hexValues.length; j++) {
                            mSPQ += hexValues[j];
                        }
                        long sPQ = Long.parseLong(mSPQ, 16);
                        tempo = (long) (60 / (sPQ / (double) 1000000));
                        System.out.println("Set tempo to: " + tempo + " BPM");
                    }
                }
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                                                                    //beats per minute go here\/
                        currentTime = (double) (event.getTick() * (60000f / (tempo * PPQ)) / 1000f);
                        times.get(trackNumber - 1).add(currentTime);
                        //System.out.println("I see at tick " + event.getTick() + " and am inserting " + currentTime);
                        //System.out.println("because my PPQ is " + PPQ + ", my tempo is " + tempo + ", and my ticks are " + event.getTick());


                        if(note <= 2) types.get(trackNumber - 1).add(0);
                        else if(note > 2 && note <= 5) types.get(trackNumber - 1).add(1);
                        else if(note > 5 && note  <= 8) types.get(trackNumber - 1).add(2);
                        else if(note > 8) types.get(trackNumber - 1).add(3);

                        /*if(currentTime <= lastTime + 0.1 && types.get(trackNumber - 1).size() > 1) {
                            if(types.get(trackNumber - 1).get(types.get(trackNumber - 1).size() - 1) ==
                                    types.get(trackNumber - 1).get(types.get(trackNumber - 1).size() - 2)) {
                                System.out.println("two notes with same time");
                                if(types.get(trackNumber - 1).get(types.get(trackNumber - 1).size() - 1) == 3) {
                                    types.get(trackNumber - 1).set(types.get(trackNumber - 1).size() - 1, 0);
                                } else {
                                    types.get(trackNumber - 1).set(types.get(trackNumber - 1).size() - 1, types.get(trackNumber - 1).get(types.get(trackNumber - 1).size() - 1) + 1);
                                }
                            }
                        }*/

                        lastTime = currentTime;

                        System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else {
                        System.out.println("Command:" + sm.getCommand());
                    }
                } else if(message instanceof javax.sound.midi.MetaMessage){
                    MetaMessage mm = (MetaMessage) message;
                    if(mm.getType() == 0x00) System.out.println("Meta message, sequence number: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() >= 0x01 && mm.getType() <= 0x0f) {
                        System.out.println("Meta message, text Event: " + Arrays.toString(byteToHex(mm.getMessage())));
                        System.out.println(mm.getMessage());
                    }
                    else if(mm.getType() == 0x20) System.out.println("Meta message, MIDI channel prefix: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() == 0x2f) System.out.println("Meta message, end of track " + trackNumber + ": " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() == 0x51); /*{
                        String[] hexValues = byteToHex(mm.getMessage());
                        String mSPQ = "";
                        System.out.println("Meta message, set Tempo: " + Arrays.toString(hexValues));
                        for(int j = 3; j < hexValues.length; j++) {
                            mSPQ += hexValues[j];
                        }
                        long sPQ = Long.parseLong(mSPQ, 16);
                        tempo = (long) (60 / (sPQ / (double) 1000000));
                    }*/
                    else if(mm.getType() == 0x54) System.out.println("Meta message, SMPTE Offset: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() == 0x58) System.out.println("Meta message, time signature: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() == 0x59) System.out.println("Meta message, key signature: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else if(mm.getType() == 0x7F) System.out.println("Meta message, Sequencer Specific Meta-Event: " + Arrays.toString(byteToHex(mm.getMessage())));
                    else System.out.println("Meta message, unknown message, message type: " + mm.getType() + ", raw message: " + mm.getMessage());
                } else {
                    System.out.println("Other message: " + message.getClass());
                }
            }

            System.out.println();
        }

        for(int i = 0; i < sequence.getTracks().length; i++) {
            System.out.println();
            for(int j = 0; j < times.get(i).size(); j++) {
                System.out.println("track: " + (i + 1) + ", time: " + times.get(i).get(j) + ", type: " + types.get(i).get(j));
                allTimes.add(times.get(i).get(j));
                allTypes.add(types.get(i).get(j));
            }
        }


        insertionSortImperative(allTimes, allTypes);

        while(checkForOverlaps(allTimes, allTypes));


        FileWriter writer = new FileWriter(outputFile);


        for(int i = 0; i < allTimes.size(); i++) {
            writer.write(allTimes.get(i) + ", " + allTypes.get(i) + "\n");
        }


        writer.close();
    }

    public static void insertionSortImperative(ArrayList<Double> input, ArrayList<Integer> types) {
        for (int i = 1; i < input.size(); i++) {
            double key = input.get(i);
            int tkey = types.get(i);
            int j = i - 1;
            while (j >= 0 && input.get(j) > key) {
                input.set(j + 1, input.get(j));
                types.set(j + 1, types.get(j));
                j = j - 1;
            }
            input.set(j + 1, key);
            types.set(j + 1, tkey);
        }
    }

    public static boolean checkForOverlaps(ArrayList<Double> allTimes, ArrayList<Integer> allTypes) {
        boolean madeASwitch = false;
        for(int i = 1; i < allTimes.size(); i++) {
            if(i < 4) continue;
            for(int j = 1; j < 4; j++) {
                if(allTimes.get(i - j) + 0.1 >= allTimes.get(i) &&
                allTypes.get(i - j) == allTypes.get(i)) {
                    madeASwitch = true;
                    if(allTypes.get(i) == 3) allTypes.set(i, 0);
                    else allTypes.set(i, allTypes.get(i) + 1);
                }
            }

        }
        return madeASwitch;
    }

    public static String[] byteToHex(byte[] nums) {
        char hexDigit1;
        char hexDigit2;
        String[] output = new String[nums.length];
        for(int i = 0; i < nums.length; i++) {
            hexDigit1 = Character.forDigit((nums[i] >> 4) & 0xF, 16);
            hexDigit2 = Character.forDigit((nums[i] & 0xF), 16);
            output[i] = hexDigit1 + "" + hexDigit2;
            output[i] = output[i].toUpperCase();
        }

        return output;

    }

}