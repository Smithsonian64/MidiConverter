import java.io.File;
import java.io.FileWriter;
import java.lang.constant.DynamicConstantDesc;
import java.util.ArrayList;
import java.util.Collections;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Test {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static void main(String[] args) throws Exception {

        ArrayList<ArrayList<Double>> times = new ArrayList<>();
        for(int i = 0; i < 2; i++) times.add(new ArrayList<>());

        ArrayList<ArrayList<Integer>> types = new ArrayList<>();
        for(int i = 0; i < 2; i++) types.add(new ArrayList<>());

        ArrayList<Double> allTimes = new ArrayList<>();
        ArrayList<Integer> allTypes = new ArrayList<>();

        Sequence sequence = MidiSystem.getSequence(new File("TTFATFMIDI.mid"));

        System.out.println(sequence.getTracks().length);


        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
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
                        times.get(trackNumber - 1).add((double) event.getTick() * (60000f / (180f * 480f)) / 1000f);

                        if(note <= 2) types.get(trackNumber - 1).add(0);
                        else if(note > 2 && note <= 5) types.get(trackNumber - 1).add(1);
                        else if(note > 5 && note  <= 8) types.get(trackNumber - 1).add(2);
                        else if(note > 8) types.get(trackNumber - 1).add(3);

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

        int cursor = 0;
        int sortedIndex = 0;
        int smallestIndex = 0;
        double smallest = 0;

        double tempTime = 0;
        int tempType = 0;

        insertionSortImperative(allTimes, allTypes);

        FileWriter writer = new FileWriter("TTFATFDef.txt");


        for(int i = 0; i < allTimes.size(); i++) {
            writer.write(allTimes.get(i) + ", " + allTypes.get(i) + "\n");
        }

        /*for(int i = 0; i < sequence.getTracks().length; i++) {
            for(int j = 0; j < times.get(i).size(); j++) {
                writer.write(times.get(i).get(j) + ", " + types.get(i).get(j) + "\n");
                //System.out.println("track: " + (i + 1) + ", time: " + times.get(i).get(j) + ", type: " + types.get(i).get(j));
            }
        }*/
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

}