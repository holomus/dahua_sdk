package org.example.dahuasdk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "middleware")
public class Middleware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "host")
    private String host;

    @Column(name = "token")
    private String token;

    @Column(name = "credentials")
    private String credentials;

    @OneToMany(mappedBy = "middleware", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Device> devices;
}