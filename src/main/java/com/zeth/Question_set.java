package com.zeth;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mt_question_set")
public class Question_set {
    @Id
    String set_no;

    @Column
    String set_description;

    @Column
    int number_of_questions;

    @Column
    String pack_no;

    public Question_set(String set_no){
        this.set_no = set_no;
    }

    public Question_set(){}

    @ManyToOne
    @JoinColumn(name = "pack_no", insertable = false, updatable = false)
    private Question_pack pack;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "set", cascade = CascadeType.ALL)
    List<Question> questions = new ArrayList<>();
}
