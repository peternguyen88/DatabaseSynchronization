package com.zeth;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mt_question_pack")
public class Question_pack {
    @Id
    String pack_no;

    @Column
    String pack_description;

    @Column
    boolean is_test_pack;

    @Column
    boolean is_practice_pack;

    public Question_pack(String pack_no){
        this.pack_no = pack_no;
    }

    public Question_pack(){}

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pack", cascade = CascadeType.ALL)
    List<Question_set> question_sets = new ArrayList<>();
}
