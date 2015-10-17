package com.github.dockerjava.core;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.AuthCmd.Exec;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CommitCmd;
import com.github.dockerjava.api.command.ContainerDiffCmd;
import com.github.dockerjava.api.command.CopyFileFromContainerCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateImageCmd;
import com.github.dockerjava.api.command.CreateImageResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectExecCmd;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PauseContainerCmd;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.command.RestartContainerCmd;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.api.command.SearchImagesCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.command.TopContainerCmd;
import com.github.dockerjava.api.command.UnpauseContainerCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.CopyFileToContainerCmd;

/**
 * Special {@link DockerCmdExecFactory} implementation that collects container and image creations while test execution
 * for the purpose of automatically cleanup.
 *
 * @author marcus
 */
public class TestDockerCmdExecFactory implements DockerCmdExecFactory {

    private List<String> containerNames = new ArrayList<String>();

    private List<String> imageNames = new ArrayList<String>();

    private DockerCmdExecFactory delegate;

    public TestDockerCmdExecFactory(DockerCmdExecFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(DockerClientConfig dockerClientConfig) {
        delegate.init(dockerClientConfig);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public CreateContainerCmd.Exec createCreateContainerCmdExec() {
        return new CreateContainerCmd.Exec() {
            @Override
            public CreateContainerResponse exec(CreateContainerCmd command) {
                CreateContainerResponse createContainerResponse = delegate.createCreateContainerCmdExec().exec(command);
                containerNames.add(createContainerResponse.getId());
                return createContainerResponse;
            }
        };
    }

    @Override
    public RemoveContainerCmd.Exec createRemoveContainerCmdExec() {
        return new RemoveContainerCmd.Exec() {
            @Override
            public Void exec(RemoveContainerCmd command) {
                delegate.createRemoveContainerCmdExec().exec(command);
                containerNames.remove(command.getContainerId());
                return null;
            }
        };
    }

    @Override
    public CreateImageCmd.Exec createCreateImageCmdExec() {
        return new CreateImageCmd.Exec() {
            @Override
            public CreateImageResponse exec(CreateImageCmd command) {
                CreateImageResponse createImageResponse = delegate.createCreateImageCmdExec().exec(command);
                imageNames.add(createImageResponse.getId());
                return createImageResponse;
            }
        };
    }

    @Override
    public RemoveImageCmd.Exec createRemoveImageCmdExec() {
        return new RemoveImageCmd.Exec() {
            @Override
            public Void exec(RemoveImageCmd command) {
                delegate.createRemoveImageCmdExec().exec(command);
                imageNames.remove(command.getImageId());
                return null;
            }
        };
    }

    @Override
    public BuildImageCmd.Exec createBuildImageCmdExec() {
        return new BuildImageCmd.Exec() {
            @Override
            public Void exec(BuildImageCmd command, ResultCallback<BuildResponseItem> resultCallback) {
                // can't detect image id here so tagging it
                String tag = command.getTag();
                if (tag == null || "".equals(tag.trim())) {
                    tag = "" + new SecureRandom().nextInt(Integer.MAX_VALUE);
                    command.withTag(tag);
                }
                delegate.createBuildImageCmdExec().exec(command, resultCallback);
                imageNames.add(tag);
                return null;
            }
        };
    }

    @Override
    public Exec createAuthCmdExec() {
        return delegate.createAuthCmdExec();
    }

    @Override
    public InfoCmd.Exec createInfoCmdExec() {
        return delegate.createInfoCmdExec();
    }

    @Override
    public PingCmd.Exec createPingCmdExec() {
        return delegate.createPingCmdExec();
    }

    @Override
    public ExecCreateCmd.Exec createExecCmdExec() {
        return delegate.createExecCmdExec();
    }

    @Override
    public VersionCmd.Exec createVersionCmdExec() {
        return delegate.createVersionCmdExec();
    }

    @Override
    public PullImageCmd.Exec createPullImageCmdExec() {
        return delegate.createPullImageCmdExec();
    }

    @Override
    public PushImageCmd.Exec createPushImageCmdExec() {
        return delegate.createPushImageCmdExec();
    }

    @Override
    public SaveImageCmd.Exec createSaveImageCmdExec() {
        return delegate.createSaveImageCmdExec();
    }

    @Override
    public SearchImagesCmd.Exec createSearchImagesCmdExec() {
        return delegate.createSearchImagesCmdExec();
    }

    @Override
    public ListImagesCmd.Exec createListImagesCmdExec() {
        return delegate.createListImagesCmdExec();
    }

    @Override
    public InspectImageCmd.Exec createInspectImageCmdExec() {
        return delegate.createInspectImageCmdExec();
    }

    @Override
    public ListContainersCmd.Exec createListContainersCmdExec() {
        return delegate.createListContainersCmdExec();
    }

    @Override
    public StartContainerCmd.Exec createStartContainerCmdExec() {
        return delegate.createStartContainerCmdExec();
    }

    @Override
    public InspectContainerCmd.Exec createInspectContainerCmdExec() {
        return delegate.createInspectContainerCmdExec();
    }

    @Override
    public WaitContainerCmd.Exec createWaitContainerCmdExec() {
        return delegate.createWaitContainerCmdExec();
    }

    @Override
    public AttachContainerCmd.Exec createAttachContainerCmdExec() {
        return delegate.createAttachContainerCmdExec();
    }

    @Override
    public ExecStartCmd.Exec createExecStartCmdExec() {
        return delegate.createExecStartCmdExec();
    }

    @Override
    public InspectExecCmd.Exec createInspectExecCmdExec() {
        return delegate.createInspectExecCmdExec();
    }

    @Override
    public LogContainerCmd.Exec createLogContainerCmdExec() {
        return delegate.createLogContainerCmdExec();
    }

    @Override
    public CopyFileFromContainerCmd.Exec createCopyFileFromContainerCmdExec() {
        return delegate.createCopyFileFromContainerCmdExec();
    }

    @Override
    public CopyFileToContainerCmd.Exec createCopyFileToContainerCmdExec() {
        return delegate.createCopyFileToContainerCmdExec();
    }

    @Override
    public StopContainerCmd.Exec createStopContainerCmdExec() {
        return delegate.createStopContainerCmdExec();
    }

    @Override
    public ContainerDiffCmd.Exec createContainerDiffCmdExec() {
        return delegate.createContainerDiffCmdExec();
    }

    @Override
    public KillContainerCmd.Exec createKillContainerCmdExec() {
        return delegate.createKillContainerCmdExec();
    }

    @Override
    public RestartContainerCmd.Exec createRestartContainerCmdExec() {
        return delegate.createRestartContainerCmdExec();
    }

    @Override
    public CommitCmd.Exec createCommitCmdExec() {
        return delegate.createCommitCmdExec();
    }

    @Override
    public TopContainerCmd.Exec createTopContainerCmdExec() {
        return delegate.createTopContainerCmdExec();
    }

    @Override
    public TagImageCmd.Exec createTagImageCmdExec() {
        return delegate.createTagImageCmdExec();
    }

    @Override
    public PauseContainerCmd.Exec createPauseContainerCmdExec() {
        return delegate.createPauseContainerCmdExec();
    }

    @Override
    public UnpauseContainerCmd.Exec createUnpauseContainerCmdExec() {
        return delegate.createUnpauseContainerCmdExec();
    }

    @Override
    public EventsCmd.Exec createEventsCmdExec() {
        return delegate.createEventsCmdExec();
    }

    @Override
    public StatsCmd.Exec createStatsCmdExec() {
        return delegate.createStatsCmdExec();
    }

    public List<String> getContainerNames() {
        return new ArrayList<String>(containerNames);
    }

    public List<String> getImageNames() {
        return new ArrayList<String>(imageNames);
    }

}
